package controllers

import java.io.{PrintWriter, StringWriter}
import java.lang.reflect.Field
import java.net.URL

import model.AS4Gateway
import play.api.libs.EventSource
import play.api.libs.iteratee.Concurrent
import play.api.libs.json._
import play.api.mvc.{Action, Controller, Result}

class LoginControl extends Controller {
  def login() = Action {
    Ok(views.html.login())
  }

  def doLogin() = Action {
    Ok
  }
}
