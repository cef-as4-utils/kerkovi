package controllers

import play.api.mvc.{Action, Controller}
import play.api.routing.JavaScriptReverseRouter
/**
 * Author: yerlibilgin
 * Date:   21/10/15.
 */
object JSRoutesController  extends Controller {

  def jsRoutes = Action { implicit request =>
    Ok(
      JavaScriptReverseRouter("jsRoutes")(
        routes.javascript.Application.gatewayMvc
      )
    ).as("text/javascript")
  }

}
