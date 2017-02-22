package controllers

import java.io.{PrintWriter, StringWriter}
import java.lang.reflect.Field
import java.net.URL
import javax.inject._

import akka.actor.ActorSystem
import model.AS4Gateway
import play.api.libs.json._
import play.api.mvc.{Controller, Result}
import utils.UserAction

class GatewayMVC @Inject()(implicit environment: play.api.Environment,
                           configuration: play.api.Configuration, actorSystem: ActorSystem) extends Controller {

  import play.api.libs.json.Json._

  def listForType(field: Field): List[JsValue] = {
    println("FIELD: " + field.getName)
    /*if (field.getName.equals("phase")) {
     return ConformancePhase.values().map(theEnum => JsObject(
        Seq("value" -> JsString(theEnum.name()),
          "label" -> JsString(theEnum.toString))
      )).toList
    } */
    List()
  }

  def gatewayMvc(id: Int, property: String, value: String = null) = UserAction { request =>
    println("POST REQUEST BODY " + request.body)
    try {
      var cls = classOf[AS4Gateway]
      var field = cls.getDeclaredField(property);
      val gateway: AS4Gateway = Databeyz.get(id)
      var editable = true

      if (value == null) {
        val json: JsValue = Json.obj(
          "editable" -> editable,
          "id" -> id,
          "value" -> field.get(gateway).toString,
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
      case th: Throwable => {
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
    } else if (nm == "boolean" || nm == "java.lang.Boolean") {
      java.lang.Boolean.valueOf(value)
    } else {
      value
    }
  }

  def newGateway() = UserAction { implicit request =>
    try {
      val json = request.body;
      val as4 = new AS4Gateway()
      as4.id = Databeyz.maxId() + 1
      val frm = json.asFormUrlEncoded;
      as4.name = frm.get("name").mkString
      as4.partyID = frm.get("partyID").mkString //json.getOrElse("", "UNKNOWN").toString
      // as4.backendAddress = frm.get("backendAddress").mkString //json.getOrElse("c2Address", "UNKNOWN").toString
      as4.mshAddress = frm.get("mshAddress").mkString //json.getOrElse("c2Address", "UNKNOWN").toString
      as4.proxyMode = false

      checkFields(as4)
    } catch {
      case th: Throwable => {
        th.printStackTrace()
        val sw = new StringWriter
        val pw = new PrintWriter(sw)
        th.printStackTrace(pw)
        BadRequest("<pre>" + sw.toString + "</pre>")
      }
    }
  }

  def checkFields(as4: AS4Gateway): Result = {
    if (as4.name == null || "" == as4.name) {
      return BadRequest("Please provide a real name")
    } else if (as4.partyID == null || "" == as4.partyID) {
      return BadRequest("Please provide a real partyid")
    } else if (as4.mshAddress == null || "" == as4.mshAddress) {
      return BadRequest("Please provide a real msh address")
    } else {
      try {
        new URL(as4.mshAddress)
      } catch {
        case th: Throwable => {
          return BadRequest("Please provide valid URL for the msh backend")
        }
      }

      if (as4.backendAddress != null && "" != as4.backendAddress)
        try {
          new URL(as4.backendAddress)
        } catch {
          case th: Throwable => {
            return BadRequest("Please provide valid backend address or leave it blank")
          }
        }

      Databeyz.add(as4)
      Ok("Your gateway has been added. It will be visible after approval.")
    }
  }
}
