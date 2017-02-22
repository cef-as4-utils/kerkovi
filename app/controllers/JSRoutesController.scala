package controllers

import play.api.mvc.{Action, Controller}
import play.api.routing.JavaScriptReverseRouter
/**
 * Author: yerlibilgin
 * Date:   21/10/15.
 */
class JSRoutesController  extends Controller {

  def jsRoutes = Action { implicit request =>
    Ok(
      JavaScriptReverseRouter("jsRoutes")(
        routes.javascript.GatewayMVC.gatewayMvc
      )
    ).as("text/javascript")
  }

}
