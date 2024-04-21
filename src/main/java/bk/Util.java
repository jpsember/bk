package bk;

import static js.base.Tools.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import bk.gen.Account;
import bk.gen.BkConfig;
import bk.gen.ShareAction;
import bk.gen.ShareInfo;
import bk.gen.Transaction;
import js.base.BasePrinter;
import js.base.DateTimeTools;
import js.data.DataUtil;
import js.data.LongArray;
import js.geometry.MyMath;
import js.json.JSMap;

public final class Util {

  public static final int BORDER_NONE = 0;
  public static final int BORDER_THIN = 1;
  public static final int BORDER_THICK = 2;
  public static final int BORDER_ROUNDED = 3;
  public static final int BORDER_TOTAL = 4;

  public static final int STYLE_NORMAL = 0;
  public static final int STYLE_INVERSE = 1;
  public static final int STYLE_MARKED = 2;
  public static final int STYLE_INVERSE_AND_MARK = 3;
  public static final int STYLE_TOTAL = 4;

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
    Random r = random();
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
  private static final DateTimeFormatter sDateFormatter;

  private static long sDefaultEpochSeconds;
  private static String sDefaultFormattedDate;

  public static long defaultEpochSeconds() {
    var x = sDefaultEpochSeconds;
    if (x == 0)
      x = sEpochSecondsToday;
    return x;
  }

  public static String defaultFormattedDate() {
    if (sDefaultFormattedDate == null)
      sDefaultFormattedDate = formatDate(defaultEpochSeconds());
    return sDefaultFormattedDate;
  }

  public static long epochSecondsToday() {
    return sEpochSecondsToday;
  }

  public static void setDefaultEpochSeconds(long x) {
    sDefaultEpochSeconds = x;
    sDefaultFormattedDate = null;
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

    // If year or month have been omitted, use previous date's
    if (pt.size() == 1) {
      pt.add(0, defaultFormattedDate().substring(5, 5 + 2));
    }
    if (pt.size() == 2)
      pt.add(0, defaultFormattedDate().substring(0, 0 + 4));

    if (pt.size() == 3) {
      var first = pt.get(0);
      if (first.length() == 2)
        pt.set(0, defaultFormattedDate().substring(0, 2) + first);

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

  public static LocalDate epochSecondsToLocalDate(long epochSeconds) {
    return Instant.ofEpochSecond(epochSeconds).atZone(sLocalTimeZoneId).toLocalDate();
  }

  public static String epochSecondsToDateString(long epochSeconds) {
    return sDateFormatter.format(epochSecondsToLocalDate(epochSeconds));
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
  }

  public static final long MAX_CURRENCY = 100_000_000_00L;

  public static String formatCurrencyEvenZero(long cents) {
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

  public static String formatCurrency(long cents) {
    if (cents == 0)
      return "";
    return formatCurrencyEvenZero(cents);
  }

  public static double currencyToDollars(long cents) {
    return cents / 100.00;
  }

  public static long dollarsToCurrency(double dollars) {
    return Math.round(dollars * 100.0);
  }

  public static long generateDate() {
    return sEpochSecondsToday + random().nextInt(31_536_000);
  }

  private static Random sRandom = new Random(1965);

  public static final LedgerField EMPTY_FIELD = new TextField("");
  public static final Validator DEFAULT_VALIDATOR = new Validator() {
  };

  public static final Validator DATE_VALIDATOR = new DateValidator();
  public static final Validator BUDGET_VALIDATOR = new CurrencyValidator().withCanBeZero(true);
  public static final Validator STOCK_VALIDATOR = new YesNoValidator();

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

  public static final FocusManager focusManager() {
    return FocusManager.SHARED_INSTANCE;
  }

  public static final FocusHandler focus() {
    return focusManager().focus();
  }

  public static String validateTransaction(Transaction t) {
    if (t.debit() == t.credit())
      return "The account numbers cannot be the same!";
    return null;
  }

  public static void createMissingAccounts(Transaction t, String optNameDr, String optNameCr) {
    for (int pass = 0; pass < 2; pass++) {
      int n = pass == 0 ? t.debit() : t.credit();
      String optName = pass == 0 ? optNameDr : optNameCr;
      createMissingAccount(n, optName);
    }
  }

  public static void createMissingAccount(int n, String optName) {
    if (account(n) == null) {
      var a = Account.newBuilder().number(n);
      a.name(optName);
      setMessageDuration(30);
      setFooterMessage("Created account:", a.name());
      storage().addOrReplace(a);
    }
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
    if (sep == 0) {
      int c1 = t1.parent() != 0 ? 1 : 0;
      int c2 = t2.parent() != 0 ? 1 : 0;
      sep = Integer.compare(c1, c2);
    }

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

  public static boolean quitCommand(KeyEvent k) {
    alert("!can't seem to use command keys reliably, so have user ctrl-c out of program");
    return false;
  }

  public static void rebuild(TransactionLedger ledger) {
    if (ledger == null)
      return;
    ledger.rebuild();
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

  /**
   * Return 0 if transaction has an account number in its debit slot, 1 if in
   * its credit slot. Throws exception if neither.
   */
  public static int debitOrCreditIndex(Transaction t, int relativeToAccount) {
    if (t.debit() == relativeToAccount)
      return 0;
    if (t.credit() == relativeToAccount)
      return 1;
    throw badArg("expected transaction to involve account #", relativeToAccount, INDENT, t);
  }

  public static Account otherAccount(Transaction t, int accountNumber) {
    Account out = null;
    if (t.debit() == accountNumber)
      out = account(t.credit());
    else if (t.credit() == accountNumber)
      out = account(t.debit());
    return out;
  }

  public static long signedAmount(Transaction t, int relativeToAccountNumber) {
    int sign = 1;
    if (t.debit() != relativeToAccountNumber) {
      if (t.credit() != relativeToAccountNumber)
        badArg("transaction does not involve account", relativeToAccountNumber, ":", INDENT, t);
      sign = -1;
    }
    return t.amount() * sign;
  }

  public static Account accountMustExist(int accountNumber) {
    var a = account(accountNumber);
    if (a == null)
      badArg("account not found:", accountNumber);
    return a;
  }

  public static ChangeManager changeManager() {
    if (sChangeManager == null)
      sChangeManager = new ChangeManager();
    return sChangeManager;
  }

  public static String accountNumberWithNameString(int optAccountNumber, String nameIfMissing) {
    if (optAccountNumber == 0)
      return "";
    var ac = account(optAccountNumber);
    if (ac == null) {
      var arg = " ???";
      if (!nullOrEmpty(nameIfMissing))
        arg = " " + nameIfMissing + arg;
      return optAccountNumber + arg;
    }
    return accountNumberWithNameString(ac);
  }

  public static String accountNumberWithNameString(Account ac) {
    return ac.number() + " " + ac.name();
  }

  private static ChangeManager sChangeManager;

  public static List<Transaction> filterOutGenerated(Collection<Transaction> trans) {
    List<Transaction> out = arrayList();
    for (var t : trans) {
      if (isGenerated(t))
        continue;
      out.add(t);
    }
    return out;
  }

  public static boolean isGenerated(Transaction t) {
    return t.parent() != 0;
  }

  public static boolean hasBudget(Account a) {
    return a.budget() != 0;
  }

  public static boolean hasBudget(int accountNumber) {
    return hasBudget(accountMustExist(accountNumber));
  }

  public static int budgetSign(int number) {
    int sign = 1;
    if (number >= 2000 && number <= 2999)
      sign = -1;
    return sign;
  }

  public static long budgetSpent(Account a) {
    int sign = budgetSign(a.number());
    var spent = sign * a.balance();
    return spent;
  }

  public static long balanceOrUnspentBudget(Account a) {
    if (hasBudget(a)) {
      todo("have enums for account classes (1xxx, 2xxx) with some helper methods");
      int sign = budgetSign(a.number());
      return a.budget() - sign * a.balance();
    }
    return a.balance();
  }

  public static int indexOfChild(Transaction t, long childId) {
    var a = LongArray.with(t.children());
    return a.indexOf(childId);
  }

  public static boolean hasChild(Transaction t, long childId) {
    return indexOfChild(t, childId) >= 0;
  }

  public static Transaction removeChild(Transaction t, long childId) {
    var a = LongArray.with(t.children());
    var i = a.indexOf(childId);
    if (i < 0)
      return t;
    a = a.toBuilder().remove(i);
    t = t.toBuilder().children(a.array()).build();
    return t;
  }

  public static boolean alertAccountDoesNotExist(int accountNumber, String message) {
    if (storage().account(accountNumber) == null) {
      var msg = "account number is zero";
      if (accountNumber != 0)
        msg = "account does not exist";
      pr("***", msg, "#:" + accountNumber, "; context:", message);
      return true;
    }
    return false;
  }

  public static Transaction addChild(Transaction t, long childId) {
    var a = LongArray.with(t.children());
    int i = a.indexOf(childId);
    if (i >= 0)
      badState("transaction already has child", childId, ":", INDENT, t);
    a = a.toBuilder().add(childId);
    t = t.toBuilder().children(a.array()).build();
    return t;
  }

  public static List<Transaction> getChildTransactions(Transaction parent) {
    if (parent.children().length == 0)
      return DataUtil.emptyList();
    List<Transaction> trs = arrayList();
    for (var timestamp : parent.children()) {
      var tr = storage().transaction(timestamp);
      if (tr == null)
        continue;
      trs.add(tr);
    }
    return trs;
  }

  public static String trimToWidth(String s, int maxWidth) {
    var dots = "..";
    int minLengthForDots = 8;
    if (s.length() > maxWidth) {
      if (maxWidth < minLengthForDots)
        return s.substring(0, maxWidth);
      return s.substring(0, maxWidth - dots.length()) + dots;
    }
    return s;
  }

  public static String extractLine(String text, int maxLineLength) {
    int j = maxLineLength;
    while (true) {
      if (j >= text.length()) {
        j = text.length();
        break;
      }
      if (text.charAt(j) <= ' ')
        break;
      if (j <= (maxLineLength * 2) / 3)
        break;
      j--;
    }
    return text.substring(0, j);
  }

  public static MessageWindow sHeader, sFooter;
  private static long sPendingDuration;
  private static long sMessageExpTime;

  public static void setMessageDuration(long seconds) {
    sPendingDuration = seconds * 1000;
  }

  public static void setFooterMessage(Object... msg) {
    var s = BasePrinter.toString(msg);
    sMessageExpTime = 0;
    if (sPendingDuration > 0)
      sMessageExpTime = System.currentTimeMillis() + sPendingDuration;
    sPendingDuration = 0;
    if (sFooter != null) {
      sFooter.setMessageAt(MessageWindow.LEFT, s);
    } else {
      pr(s);
    }
  }

  public static void updateFooterMessage() {
    if (sMessageExpTime != 0 && System.currentTimeMillis() >= sMessageExpTime) {
      setFooterMessage();
    }
  }

  public static void toggleMark(Transaction t) {
    checkState(UndoManager.SHARED_INSTANCE.live());
    var id = id(t);
    if (!sMarkedTransactionSet.remove(id))
      sMarkedTransactionSet.add(id);
  }

  public static void clearAllMarks() {
    checkState(UndoManager.SHARED_INSTANCE.live());
    sMarkedTransactionSet.clear();
  }

  public static boolean isMarked(Transaction t) {
    return sMarkedTransactionSet.contains(id(t));
  }

  /**
   * 
   * Parse a ShareInfo object from a transaction description, if possible.
   * 
   * This is how I allow a user to include share quantities within a transaction
   * for a 'stock' account.
   * 
   * Such a description has this format:
   * 
   * <pre>
   * 
   * (+|-|=)<floating point value> [<optional text for description>]
   * 
   * The prefix +,-,= indicates what type of stock transaction it is:
   * 
   * + : a share purchase
   * - : a share sale
   * = : a special transaction that changes the share balance without affecting the book value;
   *      for example, a stock split or exchange of one type of shares to another
   * 
   * </pre>
   * 
   * @return ShareInfo object, or null if there didn't appear to be one
   */
  public static ShareInfo parseShareInfo(String value) {
    final boolean db = false && alert("verbose");
    if (db)
      pr("parseShareInfo, value:", quote(value));

    var si = ShareInfo.newBuilder();
    value = value.trim();
    si.notes(value);

    do {
      // If it starts with +,-,=, or a digit, assume it is a share quantity
      if (value.isEmpty())
        break;

      var ind = "=+-".indexOf(value.charAt(0));
      if (ind < 0)
        break;
      si.action(ShareAction.values()[ind + 2]);
      // Remove the 1-char prefix
      value = value.substring(1);

      if (db)
        pr("set action:", si.action());

      String valueStr = value;
      String noteStr = "";
      var sp = value.indexOf(' ');
      if (sp >= 0) {
        valueStr = value.substring(0, sp);
        noteStr = value.substring(sp).trim();
      }

      if (db)
        pr("valueStr:", quote(valueStr), "noteStr:", quote(noteStr));
      double shares = 0;
      try {
        shares = Double.parseDouble(valueStr);
        if (db)
          pr("parsed to double:", shares);
      } catch (Throwable t) {
        if (db)
          pr("caught error:", t);
        si.action(ShareAction.ERROR);
        break;
      }
      si.shares(shares);
      si.notes(noteStr);
      if (db)
        pr("parsed:", INDENT, si);
    } while (false);
    return si.build();
  }

  public static String encodeShareInfo(ShareInfo si) {
    if (si.action() == ShareAction.ERROR)
      return "***err: " + si.notes();
    var sf = String.format("%.3f ", si.shares());
    switch (si.action()) {
    case NONE:
      return si.notes();
    case ERROR:
      return "***err: " + si.notes();
    default:
      char c = "=+-".charAt(si.action().ordinal() - 2);
      return (Character.toString(c) + sf + " " + si.notes()).trim();
    }
  }

  public static List<Long> getAllMarkedTransactions() {
    List<Long> result = arrayList();
    for (var x : sMarkedTransactionSet)
      result.add(x);
    return result;
  }

  public static Set<Long> sMarkedTransactionSet = hashSet();

  public static final int CHARS_ACCOUNT_NAME = 25;
  public static final int CHARS_DATE = 10;
  public static final int CHARS_CURRENCY = 16;
  public static final int CHARS_TRANSACTION_DESCRIPTION = 30;
  public static final int CHARS_ACCOUNT_NUMBER_AND_NAME = CHARS_ACCOUNT_NAME + 5;

  private static BkConfig sConfig;

  public static void setUtilConfig(BkConfig config) {
    sConfig = config.build();
  }

  public static BkConfig bkConfig() {
    return sConfig;
  }
}
