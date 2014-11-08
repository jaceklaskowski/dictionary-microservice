package pl.japila.dictionary

trait RuleComponent {

  case class Rule(id: Option[Long], node: String = "*", app: String = "*", obj: String = "*", mgr: String = "*", kvs: Map[String, String] = Map.empty)

  import spray.json.{DefaultJsonProtocol, JsNumber, JsObject, JsString, JsValue, RootJsonFormat}

  object RuleJsonProtocol extends DefaultJsonProtocol {

    implicit object RuleFormat extends RootJsonFormat[Rule] {
      def write(r: Rule) = {
        val fields = Map(
          "id" -> JsNumber(r.id.get),
          "node" -> JsString(r.node),
          "app" -> JsString(r.app),
          "obj" -> JsString(r.obj),
          "mgr" -> JsString(r.mgr)
        ) ++ r.kvs.map { case (k, v) => k -> JsString(v)}
        JsObject(fields)
      }

      def read(value: JsValue) = {
        val fs = value.asJsObject.fields
        val id = fs.get("id").map(_.convertTo[Long])
        val node = fs("node").convertTo[String]
        val app = fs("app").convertTo[String]
        val obj = fs("obj").convertTo[String]
        val mgr = fs("mgr").convertTo[String]
        val kvs = fs.filterKeys { key => !Set("id", "node", "app", "obj", "mgr").contains(key)}.map { case (k, v) =>
          k -> v.convertTo[String]
        }
        Rule(id, node, app, obj, mgr, kvs)
      }
    }

  }

}