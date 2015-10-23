
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/yerlibilgin/dev/eu/as4-management-console/conf/routes
// @DATE:Fri Oct 23 10:09:11 EEST 2015

package controllers;

import router.RoutesPrefix;

public class routes {
  
  public static final controllers.ReverseAs4Controller As4Controller = new controllers.ReverseAs4Controller(RoutesPrefix.byNamePrefix());
  public static final controllers.ReverseJSRoutesController JSRoutesController = new controllers.ReverseJSRoutesController(RoutesPrefix.byNamePrefix());
  public static final controllers.ReverseAssets Assets = new controllers.ReverseAssets(RoutesPrefix.byNamePrefix());
  public static final controllers.ReverseApplication Application = new controllers.ReverseApplication(RoutesPrefix.byNamePrefix());

  public static class javascript {
    
    public static final controllers.javascript.ReverseAs4Controller As4Controller = new controllers.javascript.ReverseAs4Controller(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReverseJSRoutesController JSRoutesController = new controllers.javascript.ReverseJSRoutesController(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReverseAssets Assets = new controllers.javascript.ReverseAssets(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReverseApplication Application = new controllers.javascript.ReverseApplication(RoutesPrefix.byNamePrefix());
  }

}
