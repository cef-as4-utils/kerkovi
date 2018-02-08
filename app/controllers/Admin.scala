package controllers

import java.io.{FileWriter, PrintWriter}
import javax.inject._

import akka.actor.ActorSystem
import play.api.mvc.Controller
import utils.UserAction

class Admin @Inject()(implicit environment: play.api.Environment,
                      configuration: play.api.Configuration, actorSystem: ActorSystem) extends Controller {

  def deleteGateway(id: Int) = UserAction { request =>
    Databeyz.remove(id)
    Ok("")
  }

  def undoDelete() = UserAction { request =>
    Databeyz.undoDelete();
    Ok("")
  }

  def approve(id: Int) = UserAction { implicit request =>
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

  def reject(id: Int) = UserAction { implicit request =>
    try {
      Databeyz.reject(id);
      Ok("OK");
    } catch {
      case th: Throwable => {
        th.printStackTrace();
        BadRequest(th.getMessage)
      }
    }
  }


  def admin() = UserAction { implicit request =>
    Ok(views.html.admin())
  }

  def setMode(mode: Int) = UserAction { implicit request =>

    KerkoviApplicationContext.testMode = mode
    if (mode == 0)
      KerkoviApplicationContext.currentBackendPmodes = KerkoviApplicationContext.conformancePmodes
    else
      KerkoviApplicationContext.currentBackendPmodes = KerkoviApplicationContext.interopPmodes

    val pw = new PrintWriter(new FileWriter("backendPmode.txt"))
    pw.println(mode)
    pw.close()


    Ok
  }

}
