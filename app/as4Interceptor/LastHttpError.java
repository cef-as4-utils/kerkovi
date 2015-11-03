package as4Interceptor;

import play.mvc.Controller;
import play.mvc.Result;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author: yerlibilgin
 * @date: 28/08/15.
 */
public class LastHttpError extends Controller {
  public static Exception lastHttpError;
  public static Exception lastError;
  private static final Object lastHttpErrorLock = new Object();
  private static final Object lastErrorLock = new Object();
  private static Date lastHttpErrorDate;
  private static Date lastErrorDate;
  private  static SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd  HH:mm:ss");

  public static void updateLastHttpException(Exception exception, Date date){
    synchronized (lastHttpErrorLock){
      lastHttpError = exception;
      lastHttpErrorDate = date;
    }
  }

  public static void updateLastException(Exception exception, Date date){
    synchronized (lastErrorLock){
      lastError = exception;
      lastErrorDate = date;
    }
  }

  public static Result lastHttpError(){
    synchronized (lastHttpErrorLock) {
      if (lastHttpError != null) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.write("<html><head><title>LastHttpError</title></head><body><h1>");
        pw.write("Date-Time:");
        pw.write(sdf.format(lastHttpErrorDate));
        pw.write("</h1>");
        pw.write("<pre>");
        lastHttpError.printStackTrace(pw);
        pw.write("</pre>");
        pw.write("</body></html>");
        pw.flush();
        pw.close();
        return ok(sw.toString());
      } else {
        return ok("<html><head><title>Last HTTP Error</title></head><body><h1>No Error</h1></body></html>".getBytes());
      }
    }
  }

  public static Result lastError() {
    synchronized (lastErrorLock) {
      if (lastError != null) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.write("<html><head><title>LastError</title></head><body><h1>");
        pw.write("Date-Time:");
        pw.write(sdf.format(lastErrorDate));
        pw.write("</h1>");
        pw.write("<pre>");
        lastError.printStackTrace(pw);
        pw.write("</pre>");
        pw.write("</body></html>");
        pw.flush();
        pw.close();
        return ok(sw.toString());
      } else {
        return ok("<html><head><title>Last Error</title></head><body><h1>No Error</h1></body></html>".getBytes());
      }
    }
  }
}
