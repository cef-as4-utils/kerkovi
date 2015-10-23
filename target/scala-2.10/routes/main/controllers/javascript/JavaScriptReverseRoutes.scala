
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/yerlibilgin/dev/eu/as4-management-console/conf/routes
// @DATE:Fri Oct 23 10:09:11 EEST 2015

import play.api.routing.JavaScriptReverseRoute
import play.api.mvc.{ QueryStringBindable, PathBindable, Call, JavascriptLiteral }
import play.core.routing.{ HandlerDef, ReverseRouteContext, queryString, dynamicString }


import _root_.controllers.Assets.Asset

// @LINE:6
package controllers.javascript {
  import ReverseRouteContext.empty

  // @LINE:23
  class ReverseAs4Controller(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:23
    def as4PostEndpoint: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.As4Controller.as4PostEndpoint",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "as4Endpoint"})
        }
      """
    )
  
    // @LINE:24
    def as4GetEndpoint: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.As4Controller.as4GetEndpoint",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "as4Endpoint"})
        }
      """
    )
  
  }

  // @LINE:6
  class ReverseJSRoutesController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:6
    def jsRoutes: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.JSRoutesController.jsRoutes",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "assets/javascript/routes.js"})
        }
      """
    )
  
  }

  // @LINE:21
  class ReverseAssets(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:21
    def versioned: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.Assets.versioned",
      """
        function(file) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + (""" + implicitly[PathBindable[Asset]].javascriptUnbind + """)("file", file)})
        }
      """
    )
  
  }

  // @LINE:9
  class ReverseApplication(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:10
    def main: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.Application.main",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "main"})
        }
      """
    )
  
    // @LINE:18
    def deleteGateway: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.Application.deleteGateway",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "deleteGateway" + _qS([(""" + implicitly[QueryStringBindable[Int]].javascriptUnbind + """)("id", id)])})
        }
      """
    )
  
    // @LINE:12
    def adapters: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.Application.adapters",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "adapters"})
        }
      """
    )
  
    // @LINE:14
    def kerkovi: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.Application.kerkovi",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "kerkovi"})
        }
      """
    )
  
    // @LINE:16
    def gatewayMvc: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.Application.gatewayMvc",
      """
        function(id,property,value) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "gatewayMvc" + _qS([(""" + implicitly[QueryStringBindable[Int]].javascriptUnbind + """)("id", id), (""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("property", property), (value == null ? null : (""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("value", value))])})
        }
      """
    )
  
    // @LINE:11
    def gateways: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.Application.gateways",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "gateways"})
        }
      """
    )
  
    // @LINE:9
    def index: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.Application.index",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + """"})
        }
      """
    )
  
    // @LINE:17
    def newGateway: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.Application.newGateway",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "newGateway"})
        }
      """
    )
  
    // @LINE:13
    def loremIpsum: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.Application.loremIpsum",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "loremIpsum"})
        }
      """
    )
  
    // @LINE:15
    def run: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.Application.run",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "run"})
        }
      """
    )
  
  }


}