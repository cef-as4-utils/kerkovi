package backend

import controllers.KerkoviApplicationContext
import play.api.Logger
import play.api.mvc.{Result, RawBuffer, Request}
import utils.Util
import utils.Util._

/**
  * Author: yerlibilgin
  * Date: 04/08/15.
  */
class GenericAS4Corner1 extends GenericAS4Corner {
  KerkoviApplicationContext.genericCorner1 = this
  label = "Corner1"
}