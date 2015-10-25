package controllers;

import play.GlobalSettings;
import play.mvc.Action;
import play.mvc.Http;

import java.lang.reflect.Method;

/**
 * @author: yerlibilgin
 * @date: 25/10/15.
 */
public class Global extends GlobalSettings {
  @Override
  public Action onRequest(Http.Request request, Method method) {
    return super.onRequest(request, method);
  }
}
