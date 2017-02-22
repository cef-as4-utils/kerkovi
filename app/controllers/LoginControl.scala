package controllers

import java.util
import javax.inject.{Inject, Singleton}

import play.api.Logger
import play.api.mvc.{Action, Controller}

import scala.io.Source

@Singleton
class LoginControl @Inject()() extends Controller {
  val adminMap = new util.HashMap[String, String]()
  try {
    val stream = this.getClass.getResourceAsStream("/admins.txt")
    Source.fromInputStream(stream).mkString.split("\\n").foreach {
      line => {
        val indexOf = line.indexOf(':')
        adminMap.put(line.substring(0, indexOf), line.substring(indexOf + 1))
      }
    }
    stream.close()
  } catch {
    case th: Throwable => {
      Logger.error(th.getMessage, th);
    }
  }
  def login() = Action {
    Ok(views.html.login())
  }

  def logout() = Action {
    Redirect("/login").withSession()
  }

  def doLogin() = Action { implicit request =>
    Logger.debug(request.body.asText.toString)
    val encoded = request.body.asFormUrlEncoded
    val email = encoded.get("email").mkString

    if (adminMap.containsKey(email) && adminMap.get(email)==encoded.get("password").mkString) {
      Redirect("/admin").withSession(request.session + ("email" -> email))
    } else {
      BadRequest(views.html.login("Invalid Username or password"))
    }
  }
}
