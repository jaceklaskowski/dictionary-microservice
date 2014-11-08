package pl.japila.dictionary

import akka.actor._
import akka.event.Logging
import spray.http.MediaTypes
import spray.routing._

import scala.collection.mutable

object Main extends App with SimpleRoutingApp with RuleComponent {

  implicit lazy val actorSystem = ActorSystem()

  lazy val rules = mutable.HashMap(
    0 -> Rule(Some(0)),
    1 -> Rule(Some(1)),
    10 -> Rule(Some(10), node = "10", app = "APP", obj = "OBJ", mgr = "MGR"))

  import RuleJsonProtocol._
  import spray.httpx.SprayJsonSupport._

  def addRule(rule: Rule, id: Int) = {
    val r = rule.copy(id = Some(id))
    println(s"PUT $id -> $r")
    rules += id -> r
    r
  }

  lazy val helloActor = actorSystem.actorOf(Props[HelloActor])

  lazy val helloRoute =
    get {
      path("hello") { ctx =>
        helloActor ! ctx
      }
    }

  lazy val mainRoutes =
    get {
      complete(rules.values)
    } ~
      post {
        entity(as[Rule]) { rule =>
          val id = rules.keySet.max + 1
          complete {
            addRule(rule, id)
          }
        }
      } ~
      post {
        parameters("node" ?, "count".as[Int]) { (node, count) =>
          complete(s"node=${node.getOrElse("")} count=$count")
        }
      }

  lazy val pathIntRoutes =
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
              addRule(rule, id)
            }
          }
        } ~
        delete {
          complete {
            println(s"DELETE $id")
            rules.remove(id)
          }
        }
    }

  lazy val searchGetRoutes =
    get {
      (path(Segment / Segment) & parameter("page".as[Int].?)) { (a, b, page) =>
        complete {
          s"Request received...GET /$a/$b?page=${page.getOrElse(0)}\n"
        }
      }
    }

  lazy val timerRoutes =
    (path(Segment / Segment) & entity(as[String]) & parameter("timer".as[Int].?) & put) { (a, b, body, timer) =>
      complete {
        s"Request received...PUT /$a/$b?timer=$timer\n"
      }
    }

  val route: Route =
    rejectEmptyResponse {
      pathPrefix(separateOnSlashes("api/dict/rules")) {
        logRequest("api/dict/rules", Logging.InfoLevel) {
          timerRoutes ~ searchGetRoutes ~ pathIntRoutes ~ mainRoutes
        }
      }
    }

  val interface = if (args.length == 1) args(0) else "localhost"
  val port = if (args.length == 2) args(1).toInt else 8080
  println(s"interface=$interface port=$port")
  startServer(interface, port)(route)

  class HelloActor extends Actor {
    override def receive = {
      case ctx: RequestContext => ctx.complete("Hello")
    }
  }

}
