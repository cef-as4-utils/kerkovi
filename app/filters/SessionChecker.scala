package filters

import javax.inject.{Inject, Singleton}

import akka.stream.Materializer
import controllers.{Admin, LoginControl}
import play.api.Logger
import play.api.mvc._
import play.routing.Router.Tags

import scala.concurrent.{ExecutionContext, Future}

/**
  */
@Singleton
class SessionChecker @Inject()(implicit override val mat: Materializer, ec: ExecutionContext,
                               authentication: LoginControl) extends Filter {
  override def apply(nextFilter: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = {
    if (rh.tags.get(Tags.ROUTE_CONTROLLER) == Some(classOf[Admin].getName)) {
      if (rh.secure) {
        rh.session.get("email").map { user =>
          //  the user is registered to the session, so allow him
          nextFilter(rh)
        }.getOrElse {
          if (rh.tags.contains("email")) {
            //for REST
            nextFilter(rh)
          } else {
            //no user in the session, so redirect to login
            Logger.warn("A request without session, forward to login")
            authentication.login.apply(rh).run()
          }
        }
      } else {
        Future {
          Results.BadRequest("Only HTTPS")
        }
      }
    } else {
      //an asset or the login services are called, allow!
      nextFilter(rh)
    }
  }
}
