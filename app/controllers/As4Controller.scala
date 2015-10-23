package controllers

import play.api.mvc.{EssentialAction, Action, Controller}

object As4Controller extends Controller {
  def as4GetEndpoint() = Action{ request =>
    Ok("<h3>This the AS4 endpoint, send SOAP messages please!</h3>")
  }

  def as4PostEndpoint() = Action{ request =>
    println("POST REQUEST BODY " + request.body)
    NotImplemented
  }

}
