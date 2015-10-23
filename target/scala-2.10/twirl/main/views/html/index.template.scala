
package views.html

import play.twirl.api._
import play.twirl.api.TemplateMagic._


     object index_Scope0 {
import models._
import controllers._
import play.api.i18n._
import views.html._
import play.api.templates.PlayMagic._
import play.api.mvc._
import play.api.data._

class index extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template0[play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply():play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {


Seq[Any](format.raw/*1.1*/("""<!doctype html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <title>AS4 Conformance and Interoperability Connectivity Manager</title>
    <link rel="shortcut icon" type="image/png" href=""""),_display_(/*6.55*/routes/*6.61*/.Assets.versioned("img/favicon.png")),format.raw/*6.97*/("""">
    <link rel="stylesheet" href=""""),_display_(/*7.35*/routes/*7.41*/.Assets.versioned("stylesheets/main.css")),format.raw/*7.82*/("""" />
    <link rel="stylesheet" href=""""),_display_(/*8.35*/routes/*8.41*/.Assets.versioned("stylesheets/children.css")),format.raw/*8.86*/("""" />
    <script type="text/javascript" src=""""),_display_(/*9.42*/routes/*9.48*/.Assets.versioned("js/jquery-2.1.4.min.js")),format.raw/*9.91*/(""""></script>
    <script type="text/javascript" src=""""),_display_(/*10.42*/routes/*10.48*/.JSRoutesController.jsRoutes()),format.raw/*10.78*/(""""></script>
    <script src=""""),_display_(/*11.19*/routes/*11.25*/.Assets.versioned("js/main.js")),format.raw/*11.56*/(""""></script>

    <script>
    $(function()"""),format.raw/*14.17*/("""{"""),format.raw/*14.18*/("""
      """),format.raw/*15.7*/("""initializeMainMenu($("#mainView"), $("ul.menu li > a"))
      triggerMainMenuItem("main")
    """),format.raw/*17.5*/("""}"""),format.raw/*17.6*/(""");
  </script>
  </head>
  <body>
    <table class="container" align="center">
      <tr>
        <td>
          <table><tr><td>
            <img src="img/zebra-small.png"
            height="70px"
            about="http://www.wpclipart.com/cartoon/animals/horse/horse_cartoon_zebra_T.png" alt="Zebra"/>
          </td> <td>
            <div class="title"><b>Kerkovi</b> <br/>The AS4 conformance manager</div>
          </td>
          </tr>
          </table>
          <br />
          <ul class="menu">
            <li><a href="#" page=""""),_display_(/*35.36*/routes/*35.42*/.Application.main()),format.raw/*35.61*/("""" id="main">Main</a></li>
            <li><a href="#" page=""""),_display_(/*36.36*/routes/*36.42*/.Application.run()),format.raw/*36.60*/("""" id="runTests">Run Tests</a></li>
            <li><a href="#" page=""""),_display_(/*37.36*/routes/*37.42*/.Application.gateways()),format.raw/*37.65*/("""" id="runTests">Gateways</a></li>
            <li><a href="#" page=""""),_display_(/*38.36*/routes/*38.42*/.Application.adapters()),format.raw/*38.65*/("""" id="settings">Adapters</a></li>
            <li><a href="#" page=""""),_display_(/*39.36*/routes/*39.42*/.Application.kerkovi()),format.raw/*39.64*/("""" id="kerkovi" class="kerkovi">What is kerkovi?</a></li>
          </ul>
          <div id="mainView">

          </div>

        </td>
      </tr>
    </table>

    <div id="errorDialog" class="errorDialog" title="Error">
      <table width="100%" height="100%" cellpadding="0" cellspacing="0" style="padding:3px;">
        <tr><th style="height:30px;" align="left"><span style="color:#c6d2ff;">&nbsp;Error</span></th><td align="right"><button class="bigcancel" onclick='disposeError()'>\u2718</button></td></tr>
        <tr>
          <td colspan="2">
          <div id="errorDialogContent" class="errorDialogContent">
            <h2>Welcome to Kerkovi</h2>

            <p>Click&nbsp;<strong><a href="#" onclick='triggerMainMenuItem("runTests")'>here</a></strong>
                &nbsp;to run tests on your gateway.
</p>

            <p>If you want to register your gateway for testing please go
              to <a href="#" onclick='triggerMainMenuItem("settings")'>Settings</a></strong> &nbsp;and add the&nbsp;
              <strong><em>PARTY ID</em></strong> <em>&nbsp;</em>
              and HTTP/S addresses of your gateway instances
              (one for corner 2 and one for corner 3)
</p>

          </div>
        </td>
        </tr>
      </table>
    </div>

  </body>
</html>
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
object index extends index_Scope0.index
              /*
                  -- GENERATED --
                  DATE: Fri Oct 23 10:09:11 EEST 2015
                  SOURCE: /Users/yerlibilgin/dev/eu/as4-management-console/app/views/index.scala.html
                  HASH: 2ae878f0d05356c70cc3d7d43f630667192e8aa1
                  MATRIX: 609->0|835->200|849->206|905->242|968->279|982->285|1043->326|1108->365|1122->371|1187->416|1259->462|1273->468|1336->511|1416->564|1431->570|1482->600|1539->630|1554->636|1606->667|1676->709|1705->710|1739->717|1860->811|1888->812|2457->1354|2472->1360|2512->1379|2600->1440|2615->1446|2654->1464|2751->1534|2766->1540|2810->1563|2906->1632|2921->1638|2965->1661|3061->1730|3076->1736|3119->1758
                  LINES: 25->1|30->6|30->6|30->6|31->7|31->7|31->7|32->8|32->8|32->8|33->9|33->9|33->9|34->10|34->10|34->10|35->11|35->11|35->11|38->14|38->14|39->15|41->17|41->17|59->35|59->35|59->35|60->36|60->36|60->36|61->37|61->37|61->37|62->38|62->38|62->38|63->39|63->39|63->39
                  -- GENERATED --
              */
          