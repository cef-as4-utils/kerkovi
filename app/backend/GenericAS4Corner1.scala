package backend

import controllers.Global
import esens.wp6.esensMshBackend.{SubmissionResult, SubmissionData}
import play.api.Logger

/**
 * Author: yerlibilgin
 * Date: 04/08/15.
 */
class GenericAS4Corner1 extends GenericAS4Corner {
  Global.genericCorner1 = this
  label = "Corner1"
}