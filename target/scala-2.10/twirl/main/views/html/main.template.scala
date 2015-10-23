
package views.html

import play.twirl.api._
import play.twirl.api.TemplateMagic._


     object main_Scope0 {
import models._
import controllers._
import play.api.i18n._
import views.html._
import play.api.templates.PlayMagic._
import play.api.mvc._
import play.api.data._

class main extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template0[play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply():play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {


Seq[Any](format.raw/*1.1*/("""<h2>Welcome to Kerkovi</h2>

<p>Click&nbsp;<strong><a href="#" onclick='triggerMainMenuItem("runTests")'>here</a></strong>
    &nbsp;to run tests on your gateway.
</p>

<p>If you want to register your gateway for testing please go
  to <a href="#" onclick='triggerMainMenuItem("settings")'>Settings</a></strong> &nbsp;and add the&nbsp;
  <strong><em>PARTY ID</em></strong> <em>&nbsp;</em>
  and HTTP/S addresses of your gateway instances
  (one for corner 2 and one for corner 3)
</p>
"""))
      }
    }
  }

  def render(): play.twirl.api.HtmlFormat.Appendable = apply()

  def f:(() => play.twirl.api.HtmlFormat.Appendable) = () => apply()

  def ref: this.type = this

}


}

/**/
object main extends main_Scope0.main
              /*
                  -- GENERATED --
                  DATE: Wed Oct 21 11:12:45 EEST 2015
                  SOURCE: /Users/yerlibilgin/dev/eu/as4-management-console/app/views/main.scala.html
                  HASH: 0e1e01f2204957633b1c6082c4f94562f3977f5d
                  MATRIX: 607->0
                  LINES: 25->1
                  -- GENERATED --
              */
          