package utils;

import java.util.Stack;

public class Tic {
  private static ThreadLocal<Stack<Long>> ticQueue = new ThreadLocal<>();

  public static void tic() {
    if (ticQueue.get() == null){
      ticQueue.set(new Stack<>());
    }
    ticQueue.get().push(System.currentTimeMillis());
  }

  public static long toc() {
    if (ticQueue.get() == null){
      ticQueue.set(new Stack<>());
    }

    if (!ticQueue.get().isEmpty()) {
      return System.currentTimeMillis() - ticQueue.get().pop();
    }
    return 0;
  }
}
