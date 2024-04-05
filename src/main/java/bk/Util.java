package bk;

import static js.base.Tools.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import com.googlecode.lanterna.Symbols;

import bk.gen.Account;
import bk.gen.Column;
import bk.gen.Datatype;
import bk.gen.Transaction;
import js.base.DateTimeTools;
import js.data.DataUtil;
import js.geometry.MyMath;
import js.json.JSMap;

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

  public static WinMgr winMgr() {
    return WinMgr.SHARED_INSTANCE;
  }

  public static final JSMap db(Object obj) {
    var m = map();
    if (obj == null)
      m.put("", "NULL");
    else {
      m.put("str", obj.toString());
      m.put("class", obj.getClass().getName());
    }
    return m;
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

  private static final ZoneId sLocalTimeZoneId;
  private static final List<DateTimeFormatter> sDateParsers;

  private static final long sEpochSecondsToday;
  private static final String sYearsToday;
  private static final DateTimeFormatter sDateFormatter;

  public static long epochSecondsToday() {
    return sEpochSecondsToday;
  }

  public static boolean validDate(long epochSeconds) {
    final long years20 = 31_536_000 * 20;
    long min = sEpochSecondsToday - years20;
    long max = sEpochSecondsToday + years20;
    return !(epochSeconds < min || epochSeconds > max);
  }

  public static String formatDate(long epochSeconds) {
    if (!validDate(epochSeconds)) {
      alert("invalidate date:", epochSeconds);
      return "?" + epochSeconds + "?";
    }
    checkArgument(validDate(epochSeconds), "invalid date:", epochSeconds);
    //    final long years20 = 31_536_000 * 20;
    //    long min = sEpochSecondsToday - years20;
    //    long max = sEpochSecondsToday + years20;
    //    if (epochSeconds < min || epochSeconds > max)
    //      pr("epoch seconds:", epochSeconds, "is out of range of", min, max);
    var inst = Instant.ofEpochSecond(epochSeconds).atZone(sLocalTimeZoneId);
    return sDateFormatter.format(inst);
  }

  public static long dateToEpochSeconds(String dateExpr) {
    var str = dateExpr.trim();
    if (str.isEmpty())
      return sEpochSecondsToday;

    // Replace spaces with '/'
    str = str.replace(' ', '/');
    var pt = split(str, '/');

    // If year has been omitted, add current year
    if (pt.size() == 2)
      pt.add(0, sYearsToday);

    if (pt.size() == 3) {
      var first = pt.get(0);
      if (first.length() == 2)
        pt.set(0, sYearsToday.substring(0, 2) + first);

      var second = pt.get(1).toLowerCase();
      var mi = sMonthAbbrev.indexOf(second);
      if (mi >= 0)
        pt.set(1, Integer.toString(1 + mi));
    }

    str = String.join("/", DataUtil.toStringArray(pt));

    LocalDateTime dateTime = null;
    for (var p : sDateParsers) {
      try {
        dateTime = p.parse(str, LocalDateTime::from);
        break;
      } catch (Throwable t) {
      }
    }
    long result = 0;
    if (dateTime != null) {
      var localDateTime = dateTime.atZone(sLocalTimeZoneId);
      result = localDateTime.toEpochSecond();
    }
    if (result == 0)
      badArg("Failed to parse date expression:", quote(dateExpr));
    return result;
  }

  private static final List<String> sMonthAbbrev = split("jan feb mar apr may jun jul aug sep oct nov dec",
      ' ');

  public static String epochSecondsToDateString(long epochSeconds) {
    LocalDate date = Instant.ofEpochSecond(epochSeconds).atZone(sLocalTimeZoneId).toLocalDate();
    return sDateFormatter.format(date);
  }

  static {
    // This was very helpful:  https://www.baeldung.com/java-localdate-epoch
    sLocalTimeZoneId = ZoneId.systemDefault();
    var now = LocalDate.now().atStartOfDay();
    sEpochSecondsToday = (int) now.atZone(sLocalTimeZoneId).toEpochSecond();

    {
      List<DateTimeFormatter> p = arrayList();

      p.add(new DateTimeFormatterBuilder().appendPattern("u/M/d").parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
          .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0).parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
          .toFormatter());
      p.add(new DateTimeFormatterBuilder().appendPattern("M/d")
          .parseDefaulting(ChronoField.YEAR_OF_ERA, now.getYear())
          .parseDefaulting(ChronoField.YEAR_OF_ERA, now.getYear()).parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
          .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0).parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
          .toFormatter());
      p.add(new DateTimeFormatterBuilder().appendPattern("u/MMM/dd")
          .parseDefaulting(ChronoField.YEAR_OF_ERA, now.getYear())
          .parseDefaulting(ChronoField.YEAR_OF_ERA, now.getYear()).parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
          .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0).parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
          .toFormatter());

      sDateParsers = p;

    }
    sDateFormatter = new DateTimeFormatterBuilder().appendPattern("uuuu/MM/dd").toFormatter();
    var s = epochSecondsToDateString(sEpochSecondsToday);
    sYearsToday = s.substring(0, 4);
  }

  private static final long MAX_CURRENCY = 100_000_000_00L;

  public static String formatCurrency(long cents) {
    if (cents == 0)
      return "";
    var absCents = Math.abs(cents);
    checkArgument(absCents < MAX_CURRENCY, "currency value out of range:", cents);
    var s = Long.toString(absCents);
    var k = s.length();
    int leadZeros = Math.max(0, 3 - k);
    s = "000".substring(0, leadZeros) + s;

    // Insert . and , where appropriate
    var sb = new StringBuilder(s);
    int source = sb.length() - 2;
    char c = '.';
    while (source > 0) {
      sb.insert(source, c);
      source -= 3;
      c = ',';
    }

    sb.insert(0, '$');
    if (cents < 0) {
      sb.insert(0, '(');
      sb.append(')');
    }
    return sb.toString();
  }

  public static long generateDate() {
    return sEpochSecondsToday + random().nextInt(31_536_000);
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

    /**
     * Encode a long to a string
     */
    public String encode(Object value) {
      var out = "";
      if (value != null) {
        var epochSeconds = (Long) value;
        out = epochSecondsToDateString(epochSeconds);
      }
      return out;
    }

    public ValidationResult validate(String value) {
      final boolean db = false && alert("db is on for DATE_VALIDATOR");
      if (db)
        pr("validating:", quote(value));
      long dateInSeconds = 0;
      String strDate = "";
      try {
        dateInSeconds = dateToEpochSeconds(value);
        strDate = epochSecondsToDateString(dateInSeconds);
      } catch (Throwable t) {
        if (db)
          pr("failed validating:", value, "got:", INDENT, t);
      }
      var result = new ValidationResult(strDate, dateInSeconds);
      if (db)
        pr("result:", result);
      return result;
    }
  };

  public static final Validator CURRENCY_VALIDATOR = new Validator() {
    @Override
    public String encode(Object value) {
      var out = "";
      if (value != null) {
        var i = (Long) value;
        out = formatCurrency(i);
      }
      return out;
    }

    public ValidationResult validate(String value) {
      final boolean db = false && alert("db is on");
      if (db)
        pr("validating currency:", value);
      value = value.trim();
      value = value.replace("$", "");
      value = value.replace(",", "");
      Long amount = null;
      var result = ValidationResult.NONE;
      try {
        boolean neg = false;
        if (value.startsWith("(") && value.endsWith(")")) {
          neg = true;
          value = value.substring(1, value.length() - 1);
        } else if (value.startsWith("-")) {
          neg = true;
          value = value.substring(1);
        }

        if (!value.isEmpty()) {
          int j = value.lastIndexOf('.');
          if (j < 0) {
            value = value + ".00";
          }
        }
        if (db)
          pr("parsing:", value);
        var d = Double.parseDouble(value);
        var toLong = Math.round(d * 100);
        if (toLong >= MAX_CURRENCY)
          throw badArg("failed to convert", value);
        amount = toLong;
        if (neg)
          amount = -amount;
        result = new ValidationResult(formatCurrency(amount), amount);
      } catch (Throwable t) {
        if (db)
          pr("failed to validate:", quote(value), "got:", t);
      }
      return result;
    };
  };

  public static final Validator ACCOUNT_VALIDATOR = new AccountValidator();

  public static final Validator ACCOUNT_NAME_VALIDATOR = new Validator() {
    public ValidationResult validate(String value) {
      var result = ValidationResult.NONE;
      final boolean db = false && alert("db is on");
      if (db)
        pr("validating account number:", value);

      value = value.trim();
      try {
        if (db)
          pr("parsing:", value);
        int j = value.length();
        int k = MyMath.clamp(j, 2, 30);
        if (k != j)
          throw badArg("too short or too long");
        result = new ValidationResult(value, value);
      } catch (Throwable t) {
        if (db)
          pr("failed to validate:", quote(value), "got:", t);
      }
      return result;
    };
  };

  public static final Validator DESCRIPTION_VALIDATOR = new Validator() {
    public ValidationResult validate(String value) {
      var result = ValidationResult.NONE;
      final boolean db = false && alert("db is on");
      if (db)
        pr("validating description:", quote(value));
      value = value.trim();
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
        result = new ValidationResult(value, value);
      } catch (Throwable t) {
        if (db)
          pr("failed to validate:", quote(value), "got:", t);
      }
      return result;
    };
  };

  public static final FocusManager focusManager() {
    return FocusManager.SHARED_INSTANCE;
  }

  public static final FocusHandler focus() {
    return focusManager().focus();
  }

  public static String validateTransaction(Transaction t) {
    todo("ensure accounts exist");
    if (t.debit() == t.credit())
      return "The account numbers cannot be the same!";
    for (int pass = 0; pass < 2; pass++) {
      int n = pass == 0 ? t.debit() : t.credit();
      if (account(n) == null)
        return "No such account: " + n;
    }
    return null;
  }

  public static String validateAccount(Account a) {
    return null;
  }

  public static Storage storage() {
    if (sStorage == null) {
      sStorage = new Storage();
    }
    return sStorage;
  }

  public static Account account(int number) {
    return storage().account(number);
  }

  private static Storage sStorage;

  public static final Comparator<Transaction> TRANSACTION_COMPARATOR = (t1, t2) -> {
    int sep = Long.compare(t1.date(), t2.date());
    if (sep == 0)
      sep = Integer.compare(t1.debit(), t2.debit());
    if (sep == 0)
      sep = Integer.compare(t1.credit(), t2.credit());
    if (sep == 0)
      sep = Long.compare(t1.amount(), t2.amount());
    if (sep == 0)
      sep = Long.compare(t1.timestamp(), t2.timestamp());
    return sep;
  };

  public static final Comparator<Account> ACCOUNT_COMPARATOR = (t1, t2) -> {
    return Integer.compare(t1.number(), t2.number());

  };

  public static void switchToView(KeyEvent k) {
    JWindow target = null;
    switch (k.toString()) {
    case KeyEvent.VIEW_TRANSACTIONS:
      target = sTransactionsView;
      break;
    case KeyEvent.VIEW_ACCOUNTS:
      target = sAccountsView;
      break;
    }
    if (target == null)
      return;

    focusManager().pushReplace(target);
  }

  public static boolean quitCommand(KeyEvent k) {
    alert("!can't seem to use command keys reliably, so have user ctrl-c out of program");
    return false;
  }

  public static void rebuild(TransactionLedger ledger) {
    if (ledger == null)
      return;
    ledger.rebuild();
  }

  public static void applyTransaction(Transaction t) {

  }

  public static void undoTransaction(Transaction t) {

  }

  public static JWindow sTransactionsView;
  public static JWindow sAccountsView;

  public static final int ACCOUNT_NAME_MAX_LENGTH = 30;

  public static int accountNumber(Transaction t, int index) {
    checkArgument(index >= 0 && index < 2);
    return (index == 0) ? t.debit() : t.credit();
  }

  public static int id(Account account) {
    return account.number();
  }

  public static long id(Transaction transaction) {
    return transaction.timestamp();
  }

  public static Account account(Transaction t, int index) {
    var anum = accountNumber(t, index);
    var acc = account(anum);
    return acc;
  }

  public static Account ensureNotNull(Account a, int number) {
    if (a == null)
      badState("Account with number", number, "doesn't exist");
    return a;
  }

  public static ChangeManager changeManager() {
    if (sChangeManager == null)
      sChangeManager = new ChangeManager();
    return sChangeManager;
  }

  private static ChangeManager sChangeManager;

  public static final int CHARS_ACCOUNT_NAME = 16;
  public static final int CHARS_DATE = 10;
  public static final int CHARS_CURRENCY = 13;
  public static final int CHARS_TRANSACTION_DESCRIPTION = 30;
  public static final int CHARS_ACCOUNT_NUMBER_AND_NAME = 18;

}
