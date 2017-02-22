package controllers

import java.lang.reflect.Field
import javax.inject._

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import model.AS4Gateway
import play.api.libs.EventSource
import play.api.libs.iteratee.Concurrent
import play.api.libs.json._
import play.api.libs.streams.Streams
import play.api.mvc.{Action, Controller}

class Application @Inject()(implicit environment: play.api.Environment,
                            configuration: play.api.Configuration, actorSystem: ActorSystem) extends Controller {

  val (logEnumerator, logChannel) = Concurrent.broadcast[String]
  val source = Source.fromPublisher(Streams.enumeratorToPublisher(logEnumerator))
  val materializer: ActorMaterializer = ActorMaterializer()

  /**
    * An action that provides information about the current
    * running job.
    *
    * @return
    */
  def logFeed() = Action {
    println("Log feed")
    Ok.chunked(source via EventSource.flow).as("text/event-stream")
  }

  def logFeedUpdate(log: String): Unit = {
    logChannel push log
  }


  def index(target: String) = Action {
    Ok(views.html.index(target))
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

  def decideCssForTests(gateway: AS4Gateway): String = {
    ""
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

  def visualLog() = Action {
    Ok(views.html.visualLog())
  }
}
