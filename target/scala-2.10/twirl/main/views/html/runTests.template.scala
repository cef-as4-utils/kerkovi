
package views.html

import play.twirl.api._
import play.twirl.api.TemplateMagic._


     object runTests_Scope0 {
import models._
import controllers._
import play.api.i18n._
import views.html._
import play.api.templates.PlayMagic._
import play.api.mvc._
import play.api.data._

class runTests extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template0[play.twirl.api.HtmlFormat.Appendable] {

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
  <h2>Info</h2>

  <p>There are two types of tests that should be executed on the candidate gateways. Namely <strong>ONE_WAY</strong>
    and <strong>TWO_WAY</strong>.</p>

  <p><strong>ONE_WAY&nbsp;test </strong>
    verifies that a simple&nbsp;<strong>ONE_WAY&nbsp;push MEP</strong> &nbsp;scenario is successfully achieved from <strong>Corner 1</strong>
    to <strong>Corner 2</strong>.</p>

  <p><strong>TWO_WAY test&nbsp;</strong>verifies that a simple <strong>TWO_WAY push push MEP </strong>
    scenario is successfully executed starting from <strong>Corner 1</strong> to <strong>Corner 4</strong> and all the way back to<strong> Corner 4</strong>
    .&nbsp;</p>

  <p>Kerkovi is responsible (only for connectivity testing) to submit messages to backends in order to achieve the mentioned tests.</p>

  <h2>Run</h2>

  <p>
    <label>Select gateway:</label> <select class="gateway">
  """),_display_(/*27.4*/for(gateway <- Databeyz.list()) yield /*27.35*/ {_display_(Seq[Any](format.raw/*27.37*/("""
    """),format.raw/*28.5*/("""<option value=""""),_display_(/*28.21*/gateway/*28.28*/.id),format.raw/*28.31*/("""">"""),_display_(/*28.34*/gateway/*28.41*/.name),format.raw/*28.46*/("""</option>
  """)))}),format.raw/*29.4*/("""
  """),format.raw/*30.3*/("""</select>
      <button onclick="startOneWay()" class="fancy">One Way Test</button>
      <button onclick="startTwoWay()" class="fancy">Two Way Test</button>
  </p>


  <div id="testState">

  </div>

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
object runTests extends runTests_Scope0.runTests
              /*
                  -- GENERATED --
                  DATE: Fri Oct 23 10:46:33 EEST 2015
                  SOURCE: /Users/yerlibilgin/dev/eu/as4-management-console/app/views/runTests.scala.html
                  HASH: 4733eda7daf47acb6ce049bdf134db7b4b9bf436
                  MATRIX: 615->0|665->23|693->24|724->29|782->61|809->62|1763->990|1810->1021|1850->1023|1882->1028|1925->1044|1941->1051|1965->1054|1995->1057|2011->1064|2037->1069|2080->1082|2110->1085
                  LINES: 25->1|26->2|26->2|27->3|28->4|28->4|51->27|51->27|51->27|52->28|52->28|52->28|52->28|52->28|52->28|52->28|53->29|54->30
                  -- GENERATED --
              */
          