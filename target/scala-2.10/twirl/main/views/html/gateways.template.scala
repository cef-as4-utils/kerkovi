
package views.html

import play.twirl.api._
import play.twirl.api.TemplateMagic._


     object gateways_Scope0 {
import models._
import controllers._
import play.api.i18n._
import views.html._
import play.api.templates.PlayMagic._
import play.api.mvc._
import play.api.data._

class gateways extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template0[play.twirl.api.HtmlFormat.Appendable] {

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
  <h2>Registered Gateways</h2>

  <p>The list of the AS4 compliant gateways has been given below.
    Each gateway should be provided in&nbsp;<strong>two</strong> &nbsp;instances.
    The first instance is&nbsp; <strong><em>Corner 2</em></strong> &nbsp;and the second instance is&nbsp;
    <strong><em>Corner 3</em></strong>.</p>

  <p>Click on the fields to start editing.&nbsp;</p>

  <div>
    <table class="zebra">
      <tr>
        <th class="smaller darkcolor">Name</th>
        <th class="smaller darkcolor">C2 Party ID</th> <th class="smaller darkcolor">C2 Address</th>
        <th class="smaller darkcolor">C3 Party ID</th> <th class="smaller darkcolor">C3 Address</th>
        <th class="smaller darkcolor">Test State<sup>*</sup></th>
        <th class="smaller darkcolor" title="Phase might be either connectivity or Minder">Phase<sup>**</sup></th>
        <th align="center" style="width:50px">
          <div title="Add New Gateway"><a class="bigger greenShiny" href="#" onclick="createNew()">+</a></div>
        </th>
      </tr>
      """),_display_(/*28.8*/for(gateway <- Application.registeredGateways()) yield /*28.56*/ {_display_(Seq[Any](format.raw/*28.58*/("""
        """),format.raw/*29.9*/("""<tr>
          <td>
            <div class="editable" type="text"
            binding=""""),_display_(/*32.23*/controllers/*32.34*/.routes.Application.gatewayMvc(gateway.id, "name")),format.raw/*32.84*/(""""
            >"""),_display_(/*33.15*/gateway/*33.22*/.name),format.raw/*33.27*/("""</div>
          </td>
          <td>
            <div class="editable" type="text"
            binding=""""),_display_(/*37.23*/controllers/*37.34*/.routes.Application.gatewayMvc(gateway.id, "c2PartyID")),format.raw/*37.89*/(""""
            >"""),_display_(/*38.15*/gateway/*38.22*/.c2PartyID),format.raw/*38.32*/("""</div>
          </td>
          <td>
            <div class="editable" type="text"
            binding=""""),_display_(/*42.23*/controllers/*42.34*/.routes.Application.gatewayMvc(gateway.id, "c2Address")),format.raw/*42.89*/(""""
            >"""),_display_(/*43.15*/gateway/*43.22*/.c2Address),format.raw/*43.32*/("""</div>
          </td>
          <td>
            <div class="editable" type="text"
            binding=""""),_display_(/*47.23*/controllers/*47.34*/.routes.Application.gatewayMvc(gateway.id, "c3PartyID")),format.raw/*47.89*/(""""
            >"""),_display_(/*48.15*/gateway/*48.22*/.c3PartyID),format.raw/*48.32*/("""</div>
          </td>
          <td>
            <div class="editable" type="text"
            binding=""""),_display_(/*52.23*/controllers/*52.34*/.routes.Application.gatewayMvc(gateway.id, "c3Address")),format.raw/*52.89*/(""""
            >"""),_display_(/*53.15*/gateway/*53.22*/.c3Address),format.raw/*53.32*/("""</div>
          </td>
          <td align="center">
            <div class=""""),_display_(/*56.26*/Application/*56.37*/.decideCssForTests(gateway)),format.raw/*56.64*/("""">&nbsp;</div>
          </td>
          <td>
            <div class="editable" type="text"
            binding=""""),_display_(/*60.23*/controllers/*60.34*/.routes.Application.gatewayMvc(gateway.id, "phase")),format.raw/*60.85*/(""""
            >"""),_display_(/*61.15*/gateway/*61.22*/.phase),format.raw/*61.28*/("""</div>
          </td>

          <td align="center">
            <div title="Delete"><a class="bigger shiny" href="#" onclick='deleteGateway("""),_display_(/*65.90*/gateway/*65.97*/.id),format.raw/*65.100*/(""", """"),_display_(/*65.104*/controllers/*65.115*/.routes.Application.deleteGateway(gateway.id)),format.raw/*65.160*/("""")'>-</a></div>
          </td>
        </tr>
      """)))}),format.raw/*68.8*/("""
      """),format.raw/*69.7*/("""<tr><td colspan="8">
        <p>
        <label>* The testing states are as follows:</label><br />
        <span class="notest">No test finished yet</span>
        <span class="oneway">One way test finished</span>
        <span class="twoway">Two way test finished</span>
        <span class="alltests">All tests finished</span>
        </p>
        <p>
          <label>** Phase:</label> when a gateway passes both tests, it is ready to take the conformance test. Then its <i>phase</i> should be manually set to <i>MINDER_READY</i> by the user.
        </p>
      </td></tr>

    </table>
    <div id="newGatewayDiv" style="display:none;overflow:scroll;" class="newgwclass">
      <h3>Create New Gateway</h3>
      <form id="newGatewayForm" class="fullw"><table class="fullw">
        <tr><td class="shrink"><label>Name</label></td> <td class="expand"><input type="text" name="name" /></td> </tr>
        <tr><td class="shrink"><label>C2 Party ID</label></td> <td><input type="text" name="c2PartyID" class="fullw"/></td></tr>
        <tr><td class="shrink"><label>C2 Address</label></td> <td><input type="text" name="c2Address" class="fullw"/></td></tr>
        <tr><td class="shrink"><label>C3 Party ID</label></td> <td><input type="text" name="c3PartyID" class="fullw"/></td></tr>
        <tr><td class="shrink"><label>C3 Address</label></td> <td><input type="text" name="c3Address" class="fullw"/></td></tr>
      </table>
      </form> <br />
      <table width="100%"><tr><td align="right"><button class="cancelbutton" onclick="doCancel()">\u2718 Cancel</button></td> <td align="left">
        <button class="savebutton" onclick='doCreateNew(""""),_display_(/*94.59*/controllers/*94.70*/.routes.Application.newGateway),format.raw/*94.100*/("""")'>\u2713 Save</button></td></tr>
      </table>
    </div>
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
object gateways extends gateways_Scope0.gateways
              /*
                  -- GENERATED --
                  DATE: Fri Oct 23 10:46:33 EEST 2015
                  SOURCE: /Users/yerlibilgin/dev/eu/as4-management-console/app/views/gateways.scala.html
                  HASH: 9a75df5a07630a8b7229ca3c678e0a86c0a3ea58
                  MATRIX: 615->0|665->23|693->24|724->29|782->61|809->62|1920->1147|1984->1195|2024->1197|2060->1206|2175->1294|2195->1305|2266->1355|2309->1371|2325->1378|2351->1383|2484->1489|2504->1500|2580->1555|2623->1571|2639->1578|2670->1588|2803->1694|2823->1705|2899->1760|2942->1776|2958->1783|2989->1793|3122->1899|3142->1910|3218->1965|3261->1981|3277->1988|3308->1998|3441->2104|3461->2115|3537->2170|3580->2186|3596->2193|3627->2203|3732->2281|3752->2292|3800->2319|3941->2433|3961->2444|4033->2495|4076->2511|4092->2518|4119->2524|4289->2667|4305->2674|4330->2677|4362->2681|4383->2692|4450->2737|4533->2790|4567->2797|6244->4447|6264->4458|6316->4488
                  LINES: 25->1|26->2|26->2|27->3|28->4|28->4|52->28|52->28|52->28|53->29|56->32|56->32|56->32|57->33|57->33|57->33|61->37|61->37|61->37|62->38|62->38|62->38|66->42|66->42|66->42|67->43|67->43|67->43|71->47|71->47|71->47|72->48|72->48|72->48|76->52|76->52|76->52|77->53|77->53|77->53|80->56|80->56|80->56|84->60|84->60|84->60|85->61|85->61|85->61|89->65|89->65|89->65|89->65|89->65|89->65|92->68|93->69|118->94|118->94|118->94
                  -- GENERATED --
              */
          