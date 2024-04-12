package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.io.File;
import java.util.List;

import com.googlecode.lanterna.Symbols;

import bk.gen.Account;
import bk.gen.Alignment;
import js.base.BaseObject;
import js.base.BasePrinter;
import js.file.Files;

public class PrintManager extends BaseObject {

  public static final PrintManager SHARED_INSTANCE = new PrintManager();

  public void printLedger(Account a) {
    alertVerbose();

    var ts = storage().readTransactionsForAccount(a.number());
    ts.sort(TRANSACTION_COMPARATOR);

    setMaxLength(CHARS_DATE).addCol("date");

    right().setMaxLength(CHARS_CURRENCY).addCol("debit amt");
    right().setMaxLength(CHARS_CURRENCY).addCol("credit amt");
    setMaxLength(CHARS_ACCOUNT_NUMBER_AND_NAME).stretchPct(30).addCol("other name");
    right().setMaxLength(CHARS_CURRENCY).addCol("balance");
    setMaxLength(CHARS_TRANSACTION_DESCRIPTION).stretchPct(100).addCol("description");
    //
    //    int maxCurrency = 0;
    //    int maxDesc = 0;
    //    {
    //      long currBal = 0;
    //      for (var t : ts) {
    //        currBal += t.amount();
    //        maxCurrency = Math.max(maxCurrency, formatCurrency(currBal).length());
    //        maxCurrency = Math.max(maxCurrency, formatCurrency(t.amount()).length());
    //        maxDesc = Math.max(maxDesc, t.description().length());
    //      }
    //    }

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

    //    int minDesc = Math.min(maxDesc, CHARS_TRANSACTION_DESCRIPTION);
    //
    //    int charsCurrency = Math.max(maxCurrency, 8);
    //    int extra = (CHARS_CURRENCY - charsCurrency) * 3 - 5;
    //    if (minDesc == 0)
    //      extra += 3;
    //    extra += (CHARS_TRANSACTION_DESCRIPTION - minDesc);
    //
    //    int charsDesc = 0;
    //    int ex1 = Math.max(10, extra);
    //    int charsAccountNumName = CHARS_ACCOUNT_NUMBER_AND_NAME + ex1;
    //    extra -= ex1;
    //    if (minDesc != 0) {
    //      charsDesc = minDesc;
    //      int extra2 = Math.min(maxDesc - charsDesc, extra);
    //      charsDesc += extra2;
    //      extra -= extra2;
    //    }

    //    long currBal = 0;
    mColumnSeps = true;
    long currBal = 0;
    for (var t : ts) {
      var other = otherAccount(t, a.number()).number();

      currBal += t.amount();
      beginLine();
      col(formatDate(t.date()));
      var curStr = formatCurrency(t.amount());
      if (other == t.credit())
        col(curStr).col("");
      else
        col("").col(curStr);

      col(accountNumberWithNameString(otherAccount(t, a.number())));

      col(formatCurrency(currBal));
      col(t.description());
      endLine();
    }
    renderColumns();

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
    log(mPageBuffer.toString());
    return this;
  }

  public PrintManager toDirectory(File dir) {
    mDir = Files.assertDirectoryExists(dir, "PrintManager.toDirectory");
    return this;
  }

  public PrintManager pageWidth(int numChars) {
    mMaxLineLength = numChars;
    return this;
  }

  public PrintManager pageTargetWidth(int numChars) {
    mTargetLineLength = numChars;
    return this;
  }

  private PrintManager setMaxLength(int maxLength) {
    apc().setMaxLength(maxLength);
    return this;
  }

  private PrintManager right() {
    apc().setAlignment(Alignment.RIGHT);
    return this;
  }

  private PrintManager stretchPct(int pct) {
    apc().mStretchPct = pct;
    return this;
  }

  private PrintManager shrinkPct(int pct) {
    apc().mShrinkPct = pct;
    return this;
  }

  private PrintCol apc() {
    if (mpc == null)
      mpc = new PrintCol();
    return mpc;
  }

  private PrintManager addCol(String debugName) {
    checkState(mpc != null, "no PrintCol to add for", debugName);
    mpc.setName(debugName);
    mPrintCols.add(mpc);
    mpc = null;
    return this;
  }

  private PrintManager beginLine() {
    mColNumber = 0;
    return this;
  }

  private PrintManager endLine() {
    checkState(mColNumber == mPrintCols.size(), "missing some columns");
    return this;
  }

  private PrintManager col(Object data) {
    checkState(mColNumber < mPrintCols.size(), "ran out of columns");
    var x = mPrintCols.get(mColNumber);
    x.add(data);
    mColNumber++;
    return this;
  }

  private PrintManager renderColumns() {
    determineColumnWidths();

    int numRows = mPrintCols.get(0).mText.size();

    var sb = mBuffer;

    for (int lineNumber = 0; lineNumber < numRows; lineNumber++) {
      sb.setLength(0);
      int j = INIT_INDEX;
      for (var pc : mPrintCols) {
        j++;
        String str = pc.mText.get(lineNumber);
        if (j != 0)
          sb.append(" | ");
        str = trimToWidth(str, pc.mLengthRequired);
        str = justify(str, pc.mLengthRequired, pc.mAlignment);
        sb.append(str);
      }
      cr();
    }

    return this;
  }

  private String justify(String str, int maxLength, Alignment align) {
    int extra = Math.max(0, maxLength - str.length());
    switch (align) {
    case CENTER:
      str = spaces(extra / 2) + str + spaces(extra - extra / 2);
      break;
    case LEFT:
      str = str + spaces(extra);
      break;
    case RIGHT:
      str = spaces(extra) + str;
      break;
    }
    return str;
  }

  private void determineColumnWidths() {
    int widthSum = 0;
    {
      int i = INIT_INDEX;
      for (var c : mPrintCols) {
        i++;
        if (i != 0)
          widthSum += 3; // " | "
        widthSum += c.requiredLength();
      }
    }
    if (mTargetLineLength == 0)
      mTargetLineLength = Math.min(widthSum, mMaxLineLength);

    int slack = Math.max(0, mTargetLineLength - widthSum);
    int cropAmount = Math.max(0, widthSum - mMaxLineLength);

    if (slack > 0) {
      float[] f = new float[mPrintCols.size()];
      float stretchTot = 0;
      {
        int i = INIT_INDEX;
        for (var v : mPrintCols) {
          i++;
          var x = v.mStretchPct * 100f;
          f[i] = x;
          stretchTot += x;
        }
      }
      int remain = slack;
      {
        int i = INIT_INDEX;
        for (var v : mPrintCols) {
          i++;
          var x = f[i];
          int ourChars = Math.round((x / stretchTot) * slack);
          ourChars = Math.min(ourChars, remain);
          v.adjustWidth(ourChars);
          remain -= ourChars;
        }
      }
      widthSum += slack - remain;
    } else if (cropAmount > 0) {
      todo("this can be merged with above with a little work");
      float[] f = new float[mPrintCols.size()];
      float shrinkTot = 0;
      {
        int i = INIT_INDEX;
        for (var v : mPrintCols) {
          i++;
          var x = v.mShrinkPct * 100f;
          f[i] = x;
          shrinkTot += x;
        }
      }
      int remain = slack;
      {
        int i = INIT_INDEX;
        for (var v : mPrintCols) {
          i++;
          var x = f[i];
          int ourChars = Math.round((x / shrinkTot) * slack);
          ourChars = Math.min(ourChars, remain);
          v.adjustWidth(-ourChars);
          remain -= ourChars;
        }
      }
      widthSum -= cropAmount - remain;
    }
    mLineLength = widthSum;
  }

  private StringBuilder mBuffer = new StringBuilder();
  private StringBuilder mPageBuffer = new StringBuilder();
  private int mMaxLineLength = 128;
  private int mTargetLineLength;
  private int mPendingWidth;
  private Alignment mPendingAlignment;
  private boolean mColumnSeps;
  private String mTitle = "untitled";
  private File mDir;

  private List<PrintCol> mPrintCols = arrayList();
  private PrintCol mpc;
  private int mColNumber;
  private int mLineLength;
}
