package bk;

import static js.base.Tools.*;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import js.base.BasePrinter;
import js.base.DateTimeTools;

public final class Util {
  public final static void loadUtil() {
  }

  public static void sleepMs(int ms) {
    DateTimeTools.sleepForRealMs(ms);
  }

  public static void msg(Object... args) {
    pr(">>>", BasePrinter.toString(args));
  }

  public static JScreen screen() {
    return JScreen.sharedInstance();
  }
  

  public static String randomText(int maxLength, boolean withLinefeeds) {
    StringBuilder sb = new StringBuilder();
    Random r = ThreadLocalRandom.current();
    int len = (int) Math.abs(r.nextGaussian() * maxLength);
    while (sb.length() < len) {
      int wordSize = r.nextInt(8) + 2;
      if (withLinefeeds && r.nextInt(4) == 0)
        sb.append('\n');
      else
        sb.append(' ');
      String sample = "orhxxidfusuytelrcfdlordburswfxzjfjllppdsywgsw"
          + "kvukrammvxvsjzqwplxcpkoekiznlgsgjfonlugreiqvtvpjgrqotzu";
      int cursor = r.nextInt(sample.length() - wordSize);
      sb.append(sample.substring(cursor, cursor + wordSize));
    }
    return sb.toString().trim();
  }
}
