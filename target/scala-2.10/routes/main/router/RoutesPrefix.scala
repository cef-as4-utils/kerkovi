
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/yerlibilgin/dev/eu/as4-management-console/conf/routes
// @DATE:Fri Oct 23 10:09:11 EEST 2015


package router {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}
