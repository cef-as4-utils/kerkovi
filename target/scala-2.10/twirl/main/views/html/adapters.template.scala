
package views.html

import play.twirl.api._
import play.twirl.api.TemplateMagic._


     object adapters_Scope0 {
import models._
import controllers._
import play.api.i18n._
import views.html._
import play.api.templates.PlayMagic._
import play.api.mvc._
import play.api.data._

class adapters extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template0[play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply():play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {


Seq[Any](format.raw/*1.1*/("""<script>
  $(function()"""),format.raw/*2.15*/("""{"""),format.raw/*2.16*/("""
    """),format.raw/*3.5*/("""setUpEditable($(".editable"))
  """),format.raw/*4.3*/("""}"""),format.raw/*4.4*/(""");
</script>
<div class="levha">
  <h2>Adapters</h2>

</div>
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
object adapters extends adapters_Scope0.adapters
              /*
                  -- GENERATED --
                  DATE: Fri Oct 23 10:46:33 EEST 2015
                  SOURCE: /Users/yerlibilgin/dev/eu/as4-management-console/app/views/adapters.scala.html
                  HASH: c770656be7f2b1404e84bd37cf492cb3e88a03d3
                  MATRIX: 615->0|665->23|693->24|724->29|782->61|809->62
                  LINES: 25->1|26->2|26->2|27->3|28->4|28->4
                  -- GENERATED --
              */
          