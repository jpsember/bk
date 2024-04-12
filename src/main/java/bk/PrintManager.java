package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.io.File;

import com.googlecode.lanterna.Symbols;

import bk.gen.Account;
import bk.gen.Alignment;
import js.base.BaseObject;
import js.base.BasePrinter;
import js.file.Files;

public class PrintManager extends BaseObject {

  public static final PrintManager SHARED_INSTANCE = new PrintManager();

  public void printLedger(Account a) {
    loadTools();
    loadUtil();

    var date = formatDate(epochSecondsToday());
    setTitle(a.number(), a.name(), date);
    resetPendingVars();

    a(a.number());
    a(":");
    a(a.name());
    rightJustify(mMaxLineLength - mBuffer.length() - 1);
    a(date);
    cr();
    dashes(mMaxLineLength);
    cr();

    var ts = storage().readTransactionsForAccount(a.number());
    ts.sort(TRANSACTION_COMPARATOR);

    int max = 0;
    long currBal = 0;
    for (var t : ts) {
      currBal += t.amount();
      max = Math.max(max, formatCurrency(currBal).length());
      max = Math.max(max, formatCurrency(t.amount()).length());
    }

    int charsCurrency = Math.max(max, 8);
    int extra = (CHARS_CURRENCY - charsCurrency) * 3 - 5;
    int extra2 = extra / 2;
    int charsDesc = CHARS_TRANSACTION_DESCRIPTION + extra2;
    int charsAccountNumName = CHARS_ACCOUNT_NUMBER_AND_NAME + 10 + extra2;

    currBal = 0;
    mColumnSeps = true;
    for (var t : ts) {
      var other = otherAccount(t, a.number()).number();

      currBal += t.amount();
      left(CHARS_DATE);
      a(formatDate(t.date()));

      if (other == t.credit()) {
        rightJustify(charsCurrency).a(formatCurrency(t.amount()));
        rightJustify(charsCurrency).a("");
      } else {
        rightJustify(charsCurrency).a("");
        rightJustify(charsCurrency).a(formatCurrency(t.amount()));
      }

      left(charsAccountNumName).a(accountNumberWithNameString(otherAccount(t, a.number())));
      rightJustify(charsCurrency).a(formatCurrency(currBal));
      left(charsDesc).a(t.description());
      cr();
    }
    mColumnSeps = false;
    dashes(mMaxLineLength);
    cr();
    saveToDrive();
  }

  private PrintManager left(int width) {
    mPendingWidth = width;
    mPendingAlignment = Alignment.LEFT;
    return this;
  }

  private PrintManager rightJustify(int width) {
    mPendingWidth = width;
    mPendingAlignment = Alignment.RIGHT;
    return this;
  }

  private PrintManager a(Object value) {
    var s = value.toString();

    int maxWidth = s.length();
    if (mPendingWidth >= 0)
      maxWidth = mPendingWidth;
    s = trimToWidth(s, maxWidth);

    int nsp = 0;
    switch (mPendingAlignment) {
    case CENTER:
      nsp = (maxWidth - s.length()) / 2;
      break;
    case RIGHT:
      nsp = maxWidth - s.length();
      break;
    default:
      break;
    }
    if (mBuffer.length() != 0)
      mBuffer.append(mColumnSeps ? " | " : " ");
    mBuffer.append(spaces(nsp));
    mBuffer.append(s);
    mBuffer.append(spaces(maxWidth - s.length() - nsp));

    resetPendingVars();

    return this;
  }

  public PrintManager setTitle(Object... msg) {
    var s = BasePrinter.toString(msg);
    s = sanitizeForFilename(s);
    checkArgument(!s.isEmpty(), "empty string");
    mTitle = s;
    return this;
  }

  public static String sanitizeForFilename(String s) {
    var b = new StringBuilder();
    var legal = " _-0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (legal.indexOf(c) < 0)
        c = '_';
      b.append(c);
    }
    return b.toString().trim();
  }

  private String trimToWidth(String s, int maxWidth) {
    if (s.length() > maxWidth) {
      if (maxWidth < 8)
        return s.substring(0, maxWidth);
      return s.substring(0, maxWidth - 3) + "...";
    }
    return s;
  }

  private PrintManager cr() {
    var str = mBuffer.toString();
    mBuffer.setLength(0);
    str = trimToWidth(str, mMaxLineLength);
    mPageBuffer.append(str);
    log(">>> ", str);
    mPageBuffer.append('\n');
    return this;
  }

  private void resetPendingVars() {
    mPendingAlignment = Alignment.LEFT;
    mPendingWidth = -1;
  }

  private PrintManager dashes(int count) {
    for (int i = 0; i < count; i++)
      mBuffer.append(Symbols.SINGLE_LINE_HORIZONTAL);
    return this;
  }

  public PrintManager saveToDrive() {
    var filename = checkNonEmpty(mTitle, "no title defined") + ".txt";
    if (mDir == null)
      toDirectory(Files.getDesktopDirectory());
    var f = new File(mDir, filename);
    log("saving to:", f);
    Files.S.writeString(f, mPageBuffer.toString());
    return this;
  }

  public PrintManager toDirectory(File dir) {
    mDir = Files.assertDirectoryExists(dir, "PrintManager.toDirectory");
    return this;
  }

  private StringBuilder mBuffer = new StringBuilder();
  private StringBuilder mPageBuffer = new StringBuilder();
  private int mMaxLineLength = 128;
  private int mPendingWidth;
  private Alignment mPendingAlignment;
  private boolean mColumnSeps;
  private String mTitle = "untitled";
  private File mDir;
}
