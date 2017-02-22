package utils

import play.api.Logger
import play.api.mvc._

import scala.concurrent.Future

class UserRequest[A](val username: Option[String], request: Request[A]) extends WrappedRequest[A](request)

object UserAction extends
    ActionBuilder[UserRequest] with ActionTransformer[Request, UserRequest] {
  def transform[A](request: Request[A]) = Future.successful {
    Logger.debug("username " + request.session.get("username"))
    new UserRequest(request.session.get("username"), request)
  }
}