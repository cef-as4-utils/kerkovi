package controllers

import java.io.{PrintWriter, StringWriter}
import java.lang.reflect.Field

import model.{ConformancePhase, AS4Gateway}
import play.Routes
import play.api.libs.json.Json.JsValueWrapper
import play.api.mvc.{Action, Controller}
import play.api.routing.JavaScriptReverseRouter
import play.api.libs.json._;
import scala.collection.JavaConversions._
import scala.collection.mutable

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def main() = Action {
    Ok(views.html.main())
  }

  def adapters() = Action {
    Ok(views.html.adapters())
  }

  def gateways() = Action {
    Ok(views.html.gateways())
  }

  def loremIpsum() = Action {
    Ok(views.html.loremIpsum())
  }

  def kerkovi() = Action {
    Ok(views.html.kerkovi())
  }

  def run() = Action {
    Ok(views.html.runTests())
  }


  def registeredGateways(): List[AS4Gateway] = {
    Databeyz.list()
  }

  import play.api.libs.json.Json._

  def listForType(field: Field): List[JsValue] = {
    println("FIELD: " + field.getName)
    if (field.getName.equals("phase")) {
      ConformancePhase.values().map(theEnum => JsObject(
        Seq("value" -> JsString(theEnum.name()),
          "label" -> JsString(theEnum.toString))
      )).toList
    } else {
      List()
    }
  }

  def gatewayMvc(id: Int, property: String, value: String = null) = Action { request =>
    println("POST REQUEST BODY " + request.body)
    try {
      var cls = classOf[AS4Gateway]
      var field = cls.getDeclaredField(property);

      var editable = if (field.getType.getName.contains("oolean"))
        false
      else if (field.getType.eq(classOf[ConformancePhase])) {
        val gateway = Databeyz.get(id)
        if (gateway.oneWayDone && gateway.twoWayDone)
          true
        else
          false
      } else {
        true
      }

      if (value == null) {
        val json: JsValue = Json.obj(
          "editable" -> editable,
          "id" -> id,
          "type" -> resolveType(field),
          "options" -> JsArray(listForType(field))
        )

        Ok(json)
      } else {

        //find the gateway (do it generic in the future)
        //TODO: non generic
        for (gtw <- Databeyz.list()) {
          if (gtw.id == id) {
            field.setAccessible(true)
            field.set(gtw, inferFromType(field, value));
          }
        }

        Databeyz.persist
        Ok("")
      }
    } catch {
      case th: Throwable => {
        th.printStackTrace()
        BadRequest(th.getMessage)
      }
      case _ => {
        BadRequest("Unknown")
      }
    }
  }

  def resolveType(field: Field): String = {
    val nm = field.getType.getCanonicalName
    println("Resolve type")
    println(nm)
    if (nm == "java.lang.Integer" || nm == "int") {
      "int"
    } else if (nm == "java.lang.String") {
      "string"
    } else if (nm == "model.ConformancePhase") {
      "list"
    } else if (nm == "boolean" || nm == "java.lang.Boolean") {
      "bool"
    } else {
      "string"
    }
  }

  def inferFromType(field: Field, value: String): Object = {
    val nm = field.getType.getCanonicalName
    if (nm == "java.lang.Integer" || nm == "int") {
      Integer.valueOf(value);
    } else if (nm == "java.lang.String") {
      value
    } else if (nm == "model.ConformancePhase") {
      ConformancePhase.valueOf(value)
    } else if (nm == "boolean" || nm == "java.lang.Boolean") {
      java.lang.Boolean.valueOf(value)
    } else {
      value
    }
  }

  def newGateway() = Action { implicit request =>
    try {
      val json = request.body;
      val as4 = new AS4Gateway()
      as4.id = Databeyz.maxId() + 1
      val frm = json.asFormUrlEncoded;
      as4.name = frm.get("name").mkString
      as4.c2PartyID = frm.get("c2PartyID").mkString //json.getOrElse("", "UNKNOWN").toString
      as4.c2Address = frm.get("c2Address").mkString //json.getOrElse("c2Address", "UNKNOWN").toString
      as4.c3PartyID = frm.get("c3PartyID").mkString //json.getOrElse("c3PartyID", "UNKNOWN").toString
      as4.c3Address = frm.get("c3Address").mkString //json.getOrElse("c3Address", "UNKNOWN").toString*/
      as4.phase = ConformancePhase.CONNECTIVITY
      Databeyz.add(as4)

      throw new RuntimeException("Way anam way")
      Ok("ok")
    } catch {
      case th: Throwable => {
        th.printStackTrace()
        val sw = new StringWriter
        val pw = new PrintWriter(sw)
        th.printStackTrace(pw)
        BadRequest("<pre>"+sw.toString+"</pre>")
      }
    }
  }

  def deleteGateway(id: Int) = Action { request =>
    Databeyz.remove(id)
    Ok("")
  }

  def decideCssForTests(gateway: AS4Gateway): String = {

    if (!gateway.oneWayDone && !gateway.twoWayDone) {
      "notest"
    } else if (gateway.oneWayDone && !gateway.twoWayDone) {
      "oneway"
    } else if (gateway.twoWayDone && !gateway.oneWayDone) {
      "twoway"
    } else {
      "alltests"
    }
  }
}
