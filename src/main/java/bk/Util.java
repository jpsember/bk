package bk;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.graphics.TextGraphics;

import bk.gen.Column;
import bk.gen.Datatype;
import bk.gen.Transaction;
import js.base.DateTimeTools;

public final class Util {

  public static final int BORDER_NONE = 0;
  public static final int BORDER_THIN = 1;
  public static final int BORDER_THICK = 2;
  public static final int BORDER_ROUNDED = 3;
  public static final int BORDER_TOTAL = 4;

  public static final int STYLE_NORMAL = 0;
  public static final int STYLE_INVERSE = 1;
  public static final int STYLE_TOTAL = 2;

  public final static void loadUtil() {
  }

  public static void sleepMs(int ms) {
    DateTimeTools.sleepForRealMs(ms);
  }

  public static JScreen screen() {
    return JScreen.sharedInstance();
  }

  @Deprecated
  public static TextGraphics textGraphics() {
    return screen().textGraphics();
  }

  public static WinMgr winMgr() {
    return WinMgr.SHARED_INSTANCE;
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

  public static Random random() {
    return sRandom;
  }

  public static Transaction generateTransaction() {
    var t = Transaction.newBuilder();
    t.date(generateDate());
    int amount = random().nextInt(20000);
    if (random().nextInt(8) < 1)
      amount = random().nextInt(3000000);
    t.amount(amount);
    t.debit(random().nextInt(5000) + 1000);
    t.credit(random().nextInt(5000) + 1000);
    t.description(randomText(30, false));
    return t.build();
  }

  private static ZoneId sLocalTimeZoneId = ZoneId.systemDefault();

  private static DateTimeFormatter sDateFormatter = new DateTimeFormatterBuilder().appendPattern("yyyy/MM/dd")
      .parseDefaulting(ChronoField.HOUR_OF_DAY, 0).parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
      .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0).toFormatter().withZone(ZoneId.systemDefault());

  public static String formatDate(int epochSeconds) {
    var inst = Instant.ofEpochSecond(epochSeconds);
    return sDateFormatter.format(inst);
  }

  public static int generateDate() {
    var r = random();
    var parseFormatter = sDateFormatter;
    var s = new StringBuilder();
    s.append("2024/");
    var month = r.nextInt(12) + 1;
    if (month < 10)
      s.append('0');
    s.append(month);
    s.append('/');
    var date = r.nextInt(28) + 1;
    if (date < 10)
      s.append('0');
    s.append(date);
    var str = s.toString();
    var ldate = LocalDateTime.parse(str, parseFormatter).atZone(sLocalTimeZoneId);
    var tm = ldate.toEpochSecond();
    return (int) tm;
  }

  private static Random sRandom = new Random(1965);

  public static final int WID_LEDGER = 500;
  public static final int WID_GENERAL_LEDGER = 501;
  public static final Column VERT_SEP = Column.newBuilder().datatype(Datatype.TEXT).name("").width(1).build();
  public static final LedgerField VERT_SEP_FLD = new LedgerField() {
    @Override
    public String toString() {
      return SIN;
    }

    private String SIN = Character.toString(Symbols.SINGLE_LINE_VERTICAL);
  };
}
