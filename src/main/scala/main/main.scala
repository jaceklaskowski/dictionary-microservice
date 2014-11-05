package main

import akka.actor._
import akka.event.LoggingReceive
import akka.io.IO
import spray.can.Http
import spray.http.{MediaTypes, HttpEntity, StatusCode}
import spray.http.StatusCodes._
import spray.json
import spray.json._
import spray.routing._
import spray.routing.Directives._
import spray.util.LoggingContext

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

case class ErrorResponseException(responseStatus: StatusCode, response: Option[HttpEntity]) extends Exception

class RoutedHttpService(route: Route) extends HttpServiceActor with ActorLogging {

  override implicit def actorRefFactory = context

  implicit val handler = ExceptionHandler {
    case NonFatal(ErrorResponseException(statusCode, entity)) => ctx =>
      ctx.complete(statusCode, entity)

    case NonFatal(e) => ctx => {
      log.error(e, InternalServerError.defaultMessage)
      ctx.complete(InternalServerError)
    }
  }

  def receive = LoggingReceive {
    println(">>> >>> receive called")
    runRoute(route)(handler, RejectionHandler.Default, context, RoutingSettings.default, LoggingContext.fromActorRefFactory)
  }
}

object Main extends App {

  implicit lazy val system = ActorSystem()

  case class Rule(id: Option[Long], node: String = "*", app: String = "*", obj: String = "*", mgr: String = "*", kvs: Map[String, String] = Map())

  import spray.json.DefaultJsonProtocol

  object RuleJsonProtocol extends DefaultJsonProtocol {
    implicit object RuleFormat extends RootJsonFormat[Rule] {
      def write(r: Rule) = JsObject(
        "id" -> JsNumber(r.id.get),
        "node" -> JsString(r.node),
        "app" -> JsString(r.app),
        "obj" -> JsString(r.obj),
        "mgr" -> JsString(r.mgr),
        "kvs" -> r.kvs.toJson
      )
      def read(value: JsValue) = {
        val fs = value.asJsObject.fields
        val id = fs.get("id").map(_.convertTo[Long])
        val node = fs("node").convertTo[String]
        val app = fs("app").convertTo[String]
        val obj = fs("obj").convertTo[String]
        val mgr = fs("mgr").convertTo[String]
        val kvs = fs.filterKeys { key => ! Set("id", "node", "app", "obj", "mgr").contains(key) }.map { case (k, v) =>
          k -> v.convertTo[String]
        }
        Rule(id, node, app, obj, mgr, kvs)
      }
    }
  }

  val rules = mutable.HashMap(
    0 -> Rule(Some(0)),
    1 -> Rule(Some(1)),
    10 -> Rule(Some(10), node = "10", app = "APP", obj = "OBJ", mgr = "MGR"))

  import RuleJsonProtocol._
  import spray.httpx.SprayJsonSupport._

  val route =
    rejectEmptyResponse {
      pathPrefix("api" / "dict" / "rules") {
        path(IntNumber) { id =>
          println(s">>> Received...$id")
          get {
            respondWithMediaType(MediaTypes.`application/json`) {
              complete {
                rules.get(id)
              }
            }
          } ~
            put {
              entity(as[Rule]) { rule =>
                complete {
                  val r = rule.copy(id = Some(id))
                  println(s"POST $id -> $r")
                  rules += id -> r
                  r
                }
              }
            } ~
            delete {
              complete {
                println(s"DELETE $id")
                rules.remove(id)
              }
            }
        } ~
        get {
          complete(rules.values)
        } ~
        post {
          entity(as[Rule]) { rule =>
            complete {
              val id = rules.keySet.max + 1
              val r = rule.copy(id = Some(id))
              println(s"PUT $id -> $r")
              rules += id -> r
              r
            }
          }
        }
      }
    }

  val rootService = system.actorOf(Props(new RoutedHttpService(route)))
  val host = if (args.length == 1) args(0) else "localhost"
  val port = if (args.length == 2) args(1).toInt else 8080
  println(s"host=$host port=$port")
  IO(Http) ! Http.Bind(rootService, interface = host, port = port)

  sys.addShutdownHook(system.shutdown())
}
