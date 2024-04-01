package bk;

import static js.base.Tools.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import com.googlecode.lanterna.Symbols;

import bk.gen.Column;
import bk.gen.Datatype;
import bk.gen.Transaction;
import js.base.DateTimeTools;

public final class Util {
  public static final boolean EXP = false && alert("experiment in progress");

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

//  @Deprecated
//  public static JScreen screen() {
//    return JScreen.sharedInstance();
//  }

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
  private static LocalDate sCurrentDate = LocalDate.now();
  private static DateTimeFormatter sDateFormatter = new DateTimeFormatterBuilder().appendPattern("yyyy/MM/dd")
      .parseDefaulting(ChronoField.YEAR_OF_ERA, sCurrentDate.getYear())
      .parseDefaulting(ChronoField.HOUR_OF_DAY, 0).parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
      .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0).toFormatter().withZone(ZoneId.systemDefault());

  private static DateTimeFormatter sDateParser = new DateTimeFormatterBuilder().appendPattern("[yyyy/]M/d")
      .parseDefaulting(ChronoField.YEAR_OF_ERA, sCurrentDate.getYear())
      .parseDefaulting(ChronoField.HOUR_OF_DAY, 0).parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
      .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0).toFormatter().withZone(ZoneId.systemDefault());

  public static String formatDate(int epochSeconds) {
    var inst = Instant.ofEpochSecond(epochSeconds);
    return sDateFormatter.format(inst);
  }

  private static final int MAX_CURRENCY = 10_000_000_00;

  public static String formatCurrency(int cents) {
    checkArgument(cents >= 0 && cents < MAX_CURRENCY, "currency value out of range:", cents);
    var s = Integer.toString(cents);
    var k = s.length();
    int leadZeros = Math.max(0, 3 - k);
    s = "000".substring(0, leadZeros) + s;
    var h = s.length() - 2;
    return s.substring(0, h) + "." + s.substring(h);
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

  public static final Column VERT_SEP = Column.newBuilder().datatype(Datatype.TEXT).name("").width(1).build();
  public static final LedgerField VERT_SEP_FLD = new LedgerField() {
    @Override
    public String toString() {
      return SIN;
    }

    private String SIN = Character.toString(Symbols.SINGLE_LINE_VERTICAL);
  };
  public static final LedgerField EMPTY_FIELD = new TextField("");
  public static final Validator DEFAULT_VALIDATOR = new Validator() {
  };
  public static final Validator DATE_VALIDATOR = new Validator() {
    public String validate(String value) {
      final boolean db = false && alert("db is on");
      if (db)
        pr("validating:", quote(value));
      value = value.trim();
      int dateInSeconds = 0;
      try {
        todo("clean this up; allow multiple formats");
        var temp = sDateParser.parse(value);
        Instant instant = Instant.from(temp);
        var z = instant.atZone(sLocalTimeZoneId);
        // pr("instant from parsing:", value, "is:", z.toString());
        dateInSeconds = (int) Instant.EPOCH.until(instant, ChronoUnit.SECONDS);
        var inst = Instant.ofEpochSecond(dateInSeconds).atZone(sLocalTimeZoneId);
        var dt = LocalDate.from(inst);
        var y = dt.getYear();
        if (y < 1970 || y > 2050)
          throw badArg("unexpected year:", y);
        dateInSeconds = (int) Instant.EPOCH.until(z, ChronoUnit.SECONDS);
      } catch (Throwable t) {
        if (db)
          pr("failed validating:", value, "got:", INDENT, t);
      }
      if (dateInSeconds == 0)
        return "";
      return formatDate(dateInSeconds);
    };
  };
  public static final Validator CURRENCY_VALIDATOR = new Validator() {
    public String validate(String value) {
      final boolean db = false && alert("db is on");
      if (db)
        pr("validating currency:", value);
      value = value.trim();
      int amount = -1;
      try {
        if (!value.isEmpty()) {
          int j = value.lastIndexOf('.');
          if (j < 0) {
            value = value + ".00";
          }
        }
        if (db)
          pr("parsing:", value);
        var d = Double.parseDouble(value);
        var asInt = Math.round(d * 100);
        if (asInt < 0 || asInt >= MAX_CURRENCY)
          throw badArg("failed to convert", value);
        amount = (int) asInt;
      } catch (Throwable t) {
        if (db)
          pr("failed to validate:", quote(value), "got:", t);
      }
      if (amount < 0)
        return "";

      return formatCurrency(amount);
    };
  };

  public static final Validator ACCOUNT_VALIDATOR = new Validator() {
    public String validate(String value) {
      final boolean db = false && alert("db is on");
      if (db)
        pr("validating account number:", value);
      value = value.trim();
      int number = 0;
      try {
        if (db)
          pr("parsing:", value);
        var i = Integer.parseInt(value);
        if (i < 1000 || i > 5999)
          throw badArg("unexpected account number", i);
        number = i;
      } catch (Throwable t) {
        if (db)
          pr("failed to validate:", quote(value), "got:", t);
      }
      if (number == 0)
        return "";
      return Integer.toString(number);
    };
  };

  public static final Validator DESCRIPTION_VALIDATOR = new Validator() {
    public String validate(String value) {
      final boolean db = false && alert("db is on");
      if (db)
        pr("validating description:", quote(value));
      value = value.trim();
      String desc = null;
      try {
        if (db)
          pr("parsing:", value);
        if (value.length() > 1000)
          badArg("description is too long");
        for (int i = 0; i < value.length(); i++) {
          var j = value.charAt(i);
          if (j < ' ' || j > 127)
            badArg("unexpected character:", j);
        }
        desc = value;
      } catch (Throwable t) {
        if (db)
          pr("failed to validate:", quote(value), "got:", t);
      }
      return nullTo(desc, "");
    };
  };

  public static final FocusManager focusManager() {
    return FocusManager.SHARED_INSTANCE;
  }
  public static final FocusHandler focus() {return focusManager().focus();}
}
