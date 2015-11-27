package controllers

import java.io.{PrintWriter, StringWriter}
import java.lang.reflect.Field
import java.net.URL

import model.AS4Gateway
import play.api.libs.EventSource
import play.api.libs.iteratee.Concurrent
import play.api.libs.json._
import play.api.mvc.{Result, Action, Controller}
import play.mvc.Security

object Application extends Controller {

  val (logOut, logChannel) = Concurrent.broadcast[String];

  def index = Action {
    Ok(views.html.index())
  }

  def main() = Action {
    Ok(views.html.main())
  }

  def documentation() = Action {
    Ok(views.html.documentation())
  }

  def logs() = Action {
    Ok(views.html.logs())
  }

  def gateways() = Action {
    Ok(views.html.gateways())
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
    /*if (field.getName.equals("phase")) {
     return ConformancePhase.values().map(theEnum => JsObject(
        Seq("value" -> JsString(theEnum.name()),
          "label" -> JsString(theEnum.toString))
      )).toList
    } */
    List()
  }

  def gatewayMvc(id: Int, property: String, value: String = null) = Action { request =>
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
    } else if (nm == "boolean" || nm == "java.lang.Boolean") {
      java.lang.Boolean.valueOf(value)
    } else {
      value
    }
  }

  @Security.Authenticated(classOf[Secured])
  def newGateway() = Action { implicit request =>
    try {
      val json = request.body;
      val as4 = new AS4Gateway()
      as4.id = Databeyz.maxId() + 1
      val frm = json.asFormUrlEncoded;
      as4.name = frm.get("name").mkString
      as4.partyID = frm.get("partyID").mkString //json.getOrElse("", "UNKNOWN").toString
      as4.backendAddress = frm.get("backendAddress").mkString //json.getOrElse("c2Address", "UNKNOWN").toString
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
        case _ => {
          return BadRequest("Please provide valid URL for the msh backend")
        }
      }

      if (as4.backendAddress != null && "" != as4.backendAddress)
        try {
          new URL(as4.backendAddress)
        } catch {
          case _ => {
            return BadRequest("Please provide valid backend address or leave it blank")
          }
        }

      Databeyz.add(as4)
      Ok("Your gateway has been added. It will be visible after approval.")
    }
  }

  def deleteGateway(id: Int) = Action { request =>
    Databeyz.remove(id)
    Ok("")
  }

  def undoDelete() = Action { request =>
    Databeyz.undoDelete();
    Ok("")
  }

  def decideCssForTests(gateway: AS4Gateway): String = {
    ""
  }

  /**
    * An action that provides information about the current
    * running job.
    * @return
    */
  def logFeed() = Action {
    println("Log feed")
    Ok.chunked(logOut &> EventSource()).as("text/event-stream")
  }

  def logFeedUpdate(log: String): Unit = {
    logChannel.push(log)
  }

  @Security.Authenticated(classOf[Secured])
  def approve(id: Int) = Action { implicit request =>
    try {
      Databeyz.approve(id);
      Ok("OK");
    } catch {
      case th: Throwable => {
        th.printStackTrace();
        BadRequest(th.getMessage)
      }
    }
  }

  @Security.Authenticated(classOf[Secured])
  def approvePage() = Action { implicit request =>
    Ok(views.html.approve())
  }

  def showAS4Message(time: Long) = Action { implicit request =>
    Ok(LogDB.readAs4Message(time))
  }

  def showReply(time: Long) = Action { implicit request =>
    Ok(LogDB.readReply(time))
  }

  def showException(time: Long) = Action { implicit request =>
    Ok(LogDB.readException(time))
  }

  def prevLogPage(currentPage: Int) = Action { implicit request =>
    var page = currentPage

    if (page > 0)
      page = page - 1

    Ok(views.html.logTableView.render(page));
  }

  def nextLogPage(currentPage: Int) = Action { implicit request =>
    var page = currentPage

    if (page < LogDB.list.size() / LogDB.PAGE_SIZE)
      page = page + 1

    Ok(views.html.logTableView.render(page));
  }
}
