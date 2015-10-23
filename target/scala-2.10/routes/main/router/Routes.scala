
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/yerlibilgin/dev/eu/as4-management-console/conf/routes
// @DATE:Fri Oct 23 10:09:11 EEST 2015

package router

import play.core.routing._
import play.core.routing.HandlerInvokerFactory._
import play.core.j._

import play.api.mvc._

import _root_.controllers.Assets.Asset

object Routes extends Routes

class Routes extends GeneratedRouter {

  import ReverseRouteContext.empty

  override val errorHandler: play.api.http.HttpErrorHandler = play.api.http.LazyHttpErrorHandler

  private var _prefix = "/"

  def withPrefix(prefix: String): Routes = {
    _prefix = prefix
    router.RoutesPrefix.setPrefix(prefix)
    
    this
  }

  def prefix: String = _prefix

  lazy val defaultPrefix: String = {
    if (this.prefix.endsWith("/")) "" else "/"
  }

  def documentation: Seq[(String, String, String)] = List(
    ("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """assets/javascript/routes.js""", """controllers.JSRoutesController.jsRoutes"""),
    ("""GET""", prefix, """controllers.Application.index()"""),
    ("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """main""", """controllers.Application.main()"""),
    ("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """gateways""", """controllers.Application.gateways()"""),
    ("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """adapters""", """controllers.Application.adapters()"""),
    ("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """loremIpsum""", """controllers.Application.loremIpsum()"""),
    ("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """kerkovi""", """controllers.Application.kerkovi()"""),
    ("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """run""", """controllers.Application.run()"""),
    ("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """gatewayMvc""", """controllers.Application.gatewayMvc(id:Int, property:String, value:String ?= null)"""),
    ("""POST""", prefix + (if(prefix.endsWith("/")) "" else "/") + """newGateway""", """controllers.Application.newGateway()"""),
    ("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """deleteGateway""", """controllers.Application.deleteGateway(id:Int)"""),
    ("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """$file<.+>""", """controllers.Assets.versioned(path:String = "/public", file:Asset)"""),
    ("""POST""", prefix + (if(prefix.endsWith("/")) "" else "/") + """as4Endpoint""", """controllers.As4Controller.as4PostEndpoint()"""),
    ("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """as4Endpoint""", """controllers.As4Controller.as4GetEndpoint()"""),
    Nil
  ).foldLeft(List.empty[(String,String,String)]) { (s,e) => e.asInstanceOf[Any] match {
    case r @ (_,_,_) => s :+ r.asInstanceOf[(String,String,String)]
    case l => s ++ l.asInstanceOf[List[(String,String,String)]]
  }}


  // @LINE:6
  private[this] lazy val controllers_JSRoutesController_jsRoutes0_route: Route.ParamsExtractor = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("assets/javascript/routes.js")))
  )
  private[this] lazy val controllers_JSRoutesController_jsRoutes0_invoker = createInvoker(
    controllers.JSRoutesController.jsRoutes,
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.JSRoutesController",
      "jsRoutes",
      Nil,
      "GET",
      """""",
      this.prefix + """assets/javascript/routes.js"""
    )
  )

  // @LINE:9
  private[this] lazy val controllers_Application_index1_route: Route.ParamsExtractor = Route("GET",
    PathPattern(List(StaticPart(this.prefix)))
  )
  private[this] lazy val controllers_Application_index1_invoker = createInvoker(
    controllers.Application.index(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Application",
      "index",
      Nil,
      "GET",
      """ Home page""",
      this.prefix + """"""
    )
  )

  // @LINE:10
  private[this] lazy val controllers_Application_main2_route: Route.ParamsExtractor = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("main")))
  )
  private[this] lazy val controllers_Application_main2_invoker = createInvoker(
    controllers.Application.main(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Application",
      "main",
      Nil,
      "GET",
      """""",
      this.prefix + """main"""
    )
  )

  // @LINE:11
  private[this] lazy val controllers_Application_gateways3_route: Route.ParamsExtractor = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("gateways")))
  )
  private[this] lazy val controllers_Application_gateways3_invoker = createInvoker(
    controllers.Application.gateways(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Application",
      "gateways",
      Nil,
      "GET",
      """""",
      this.prefix + """gateways"""
    )
  )

  // @LINE:12
  private[this] lazy val controllers_Application_adapters4_route: Route.ParamsExtractor = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("adapters")))
  )
  private[this] lazy val controllers_Application_adapters4_invoker = createInvoker(
    controllers.Application.adapters(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Application",
      "adapters",
      Nil,
      "GET",
      """""",
      this.prefix + """adapters"""
    )
  )

  // @LINE:13
  private[this] lazy val controllers_Application_loremIpsum5_route: Route.ParamsExtractor = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("loremIpsum")))
  )
  private[this] lazy val controllers_Application_loremIpsum5_invoker = createInvoker(
    controllers.Application.loremIpsum(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Application",
      "loremIpsum",
      Nil,
      "GET",
      """""",
      this.prefix + """loremIpsum"""
    )
  )

  // @LINE:14
  private[this] lazy val controllers_Application_kerkovi6_route: Route.ParamsExtractor = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("kerkovi")))
  )
  private[this] lazy val controllers_Application_kerkovi6_invoker = createInvoker(
    controllers.Application.kerkovi(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Application",
      "kerkovi",
      Nil,
      "GET",
      """""",
      this.prefix + """kerkovi"""
    )
  )

  // @LINE:15
  private[this] lazy val controllers_Application_run7_route: Route.ParamsExtractor = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("run")))
  )
  private[this] lazy val controllers_Application_run7_invoker = createInvoker(
    controllers.Application.run(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Application",
      "run",
      Nil,
      "GET",
      """""",
      this.prefix + """run"""
    )
  )

  // @LINE:16
  private[this] lazy val controllers_Application_gatewayMvc8_route: Route.ParamsExtractor = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("gatewayMvc")))
  )
  private[this] lazy val controllers_Application_gatewayMvc8_invoker = createInvoker(
    controllers.Application.gatewayMvc(fakeValue[Int], fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Application",
      "gatewayMvc",
      Seq(classOf[Int], classOf[String], classOf[String]),
      "GET",
      """""",
      this.prefix + """gatewayMvc"""
    )
  )

  // @LINE:17
  private[this] lazy val controllers_Application_newGateway9_route: Route.ParamsExtractor = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("newGateway")))
  )
  private[this] lazy val controllers_Application_newGateway9_invoker = createInvoker(
    controllers.Application.newGateway(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Application",
      "newGateway",
      Nil,
      "POST",
      """""",
      this.prefix + """newGateway"""
    )
  )

  // @LINE:18
  private[this] lazy val controllers_Application_deleteGateway10_route: Route.ParamsExtractor = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("deleteGateway")))
  )
  private[this] lazy val controllers_Application_deleteGateway10_invoker = createInvoker(
    controllers.Application.deleteGateway(fakeValue[Int]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Application",
      "deleteGateway",
      Seq(classOf[Int]),
      "GET",
      """""",
      this.prefix + """deleteGateway"""
    )
  )

  // @LINE:21
  private[this] lazy val controllers_Assets_versioned11_route: Route.ParamsExtractor = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), DynamicPart("file", """.+""",false)))
  )
  private[this] lazy val controllers_Assets_versioned11_invoker = createInvoker(
    controllers.Assets.versioned(fakeValue[String], fakeValue[Asset]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Assets",
      "versioned",
      Seq(classOf[String], classOf[Asset]),
      "GET",
      """ Map the JS resource paths""",
      this.prefix + """$file<.+>"""
    )
  )

  // @LINE:23
  private[this] lazy val controllers_As4Controller_as4PostEndpoint12_route: Route.ParamsExtractor = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("as4Endpoint")))
  )
  private[this] lazy val controllers_As4Controller_as4PostEndpoint12_invoker = createInvoker(
    controllers.As4Controller.as4PostEndpoint(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.As4Controller",
      "as4PostEndpoint",
      Nil,
      "POST",
      """""",
      this.prefix + """as4Endpoint"""
    )
  )

  // @LINE:24
  private[this] lazy val controllers_As4Controller_as4GetEndpoint13_route: Route.ParamsExtractor = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("as4Endpoint")))
  )
  private[this] lazy val controllers_As4Controller_as4GetEndpoint13_invoker = createInvoker(
    controllers.As4Controller.as4GetEndpoint(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.As4Controller",
      "as4GetEndpoint",
      Nil,
      "GET",
      """""",
      this.prefix + """as4Endpoint"""
    )
  )


  def routes: PartialFunction[RequestHeader, Handler] = {
  
    // @LINE:6
    case controllers_JSRoutesController_jsRoutes0_route(params) =>
      call { 
        controllers_JSRoutesController_jsRoutes0_invoker.call(controllers.JSRoutesController.jsRoutes)
      }
  
    // @LINE:9
    case controllers_Application_index1_route(params) =>
      call { 
        controllers_Application_index1_invoker.call(controllers.Application.index())
      }
  
    // @LINE:10
    case controllers_Application_main2_route(params) =>
      call { 
        controllers_Application_main2_invoker.call(controllers.Application.main())
      }
  
    // @LINE:11
    case controllers_Application_gateways3_route(params) =>
      call { 
        controllers_Application_gateways3_invoker.call(controllers.Application.gateways())
      }
  
    // @LINE:12
    case controllers_Application_adapters4_route(params) =>
      call { 
        controllers_Application_adapters4_invoker.call(controllers.Application.adapters())
      }
  
    // @LINE:13
    case controllers_Application_loremIpsum5_route(params) =>
      call { 
        controllers_Application_loremIpsum5_invoker.call(controllers.Application.loremIpsum())
      }
  
    // @LINE:14
    case controllers_Application_kerkovi6_route(params) =>
      call { 
        controllers_Application_kerkovi6_invoker.call(controllers.Application.kerkovi())
      }
  
    // @LINE:15
    case controllers_Application_run7_route(params) =>
      call { 
        controllers_Application_run7_invoker.call(controllers.Application.run())
      }
  
    // @LINE:16
    case controllers_Application_gatewayMvc8_route(params) =>
      call(params.fromQuery[Int]("id", None), params.fromQuery[String]("property", None), params.fromQuery[String]("value", Some(null))) { (id, property, value) =>
        controllers_Application_gatewayMvc8_invoker.call(controllers.Application.gatewayMvc(id, property, value))
      }
  
    // @LINE:17
    case controllers_Application_newGateway9_route(params) =>
      call { 
        controllers_Application_newGateway9_invoker.call(controllers.Application.newGateway())
      }
  
    // @LINE:18
    case controllers_Application_deleteGateway10_route(params) =>
      call(params.fromQuery[Int]("id", None)) { (id) =>
        controllers_Application_deleteGateway10_invoker.call(controllers.Application.deleteGateway(id))
      }
  
    // @LINE:21
    case controllers_Assets_versioned11_route(params) =>
      call(Param[String]("path", Right("/public")), params.fromPath[Asset]("file", None)) { (path, file) =>
        controllers_Assets_versioned11_invoker.call(controllers.Assets.versioned(path, file))
      }
  
    // @LINE:23
    case controllers_As4Controller_as4PostEndpoint12_route(params) =>
      call { 
        controllers_As4Controller_as4PostEndpoint12_invoker.call(controllers.As4Controller.as4PostEndpoint())
      }
  
    // @LINE:24
    case controllers_As4Controller_as4GetEndpoint13_route(params) =>
      call { 
        controllers_As4Controller_as4GetEndpoint13_invoker.call(controllers.As4Controller.as4GetEndpoint())
      }
  }
}