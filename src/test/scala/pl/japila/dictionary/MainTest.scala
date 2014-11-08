package pl.japila.dictionary

import org.specs2._
import spray.http.MediaTypes
import spray.routing.HttpService
import spray.testkit.Specs2RouteTest

class MainTest extends mutable.Specification with Specs2RouteTest with HttpService with RuleComponent {
  def actorRefFactory = system

  "The uservice" should {
    "return a list of available rules for GET requests to the root path" in {
      Get() ~> Main.mainRoutes ~> check {
        mediaType === MediaTypes.`application/json`

        import RuleJsonProtocol._
        import spray.httpx.SprayJsonSupport._
        import spray.httpx.unmarshalling._
        val Right(rules) = response.entity.as[Seq[Rule]]

        rules.size === 3
        rules.contains(Rule(Some(0)))
        rules.contains(Rule(Some(1)))
        rules.contains(Rule(Some(10), node = "10", app = "APP", obj = "OBJ", mgr = "MGR"))
      }
    }
  }
}
