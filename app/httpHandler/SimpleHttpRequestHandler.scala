package httpHandler;

import javax.inject.Inject
import play.api.http._
import play.api.mvc._
import play.api.routing.Router

class SimpleHttpRequestHandler @Inject() (router: Router) extends HttpRequestHandler {
  def handlerForRequest(request: RequestHeader) = {
    router.routes.lift(request) match {
      case Some(handler) => {
        println(request.path)
        (request, handler)
      }
      case None => (request, Action(Results.Ok("Not found")))
    }
  }
}
