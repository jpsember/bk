package bk;

import js.base.BasePrinter;
import js.base.DateTimeTools;
import static js.base.Tools.*;

public final class Util {
  public final static void loadUtil() {
  }

  public static void sleepMs(int ms) {
    DateTimeTools.sleepForRealMs(ms);
  }

  public static void msg(Object... args) {
    pr(">>>", BasePrinter.toString(args));
  }

}
