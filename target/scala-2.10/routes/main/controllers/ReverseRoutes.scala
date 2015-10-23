
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/yerlibilgin/dev/eu/as4-management-console/conf/routes
// @DATE:Fri Oct 23 10:09:11 EEST 2015

import play.api.mvc.{ QueryStringBindable, PathBindable, Call, JavascriptLiteral }
import play.core.routing.{ HandlerDef, ReverseRouteContext, queryString, dynamicString }


import _root_.controllers.Assets.Asset

// @LINE:6
package controllers {

  // @LINE:23
  class ReverseAs4Controller(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:23
    def as4PostEndpoint(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "as4Endpoint")
    }
  
    // @LINE:24
    def as4GetEndpoint(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "as4Endpoint")
    }
  
  }

  // @LINE:6
  class ReverseJSRoutesController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:6
    def jsRoutes(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "assets/javascript/routes.js")
    }
  
  }

  // @LINE:21
  class ReverseAssets(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:21
    def versioned(file:Asset): Call = {
      implicit val _rrc = new ReverseRouteContext(Map(("path", "/public")))
      Call("GET", _prefix + { _defaultPrefix } + implicitly[PathBindable[Asset]].unbind("file", file))
    }
  
  }

  // @LINE:9
  class ReverseApplication(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:10
    def main(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "main")
    }
  
    // @LINE:18
    def deleteGateway(id:Int): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "deleteGateway" + queryString(List(Some(implicitly[QueryStringBindable[Int]].unbind("id", id)))))
    }
  
    // @LINE:12
    def adapters(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "adapters")
    }
  
    // @LINE:14
    def kerkovi(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "kerkovi")
    }
  
    // @LINE:16
    def gatewayMvc(id:Int, property:String, value:String = null): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "gatewayMvc" + queryString(List(Some(implicitly[QueryStringBindable[Int]].unbind("id", id)), Some(implicitly[QueryStringBindable[String]].unbind("property", property)), if(value == null) None else Some(implicitly[QueryStringBindable[String]].unbind("value", value)))))
    }
  
    // @LINE:11
    def gateways(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "gateways")
    }
  
    // @LINE:9
    def index(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix)
    }
  
    // @LINE:17
    def newGateway(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "newGateway")
    }
  
    // @LINE:13
    def loremIpsum(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "loremIpsum")
    }
  
    // @LINE:15
    def run(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "run")
    }
  
  }


}