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
    //    alertVerbose();
    init();

    var ts = storage().readTransactionsForAccount(a.number());
    ts.sort(TRANSACTION_COMPARATOR);

    setMaxLength(CHARS_DATE).addCol("date");

    right().setMaxLength(CHARS_CURRENCY).addCol("debit amt");
    right().setMaxLength(CHARS_CURRENCY).addCol("credit amt");
    setMaxLength(CHARS_ACCOUNT_NUMBER_AND_NAME).stretchPct(30).addCol("other name");
    right().setMaxLength(CHARS_CURRENCY).addCol("balance");
    setMaxLength(CHARS_TRANSACTION_DESCRIPTION).stretchPct(100).addCol("description");

    var date = formatDate(epochSecondsToday());
    setTitle(a.number(), a.name(), date);

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

    determineColumnWidths();

    {
      var s = mBuffer;
      s.append(a.number());
      s.append(" : ");
      s.append(a.name());
      s.append(justify(date, mLineLength - s.length(), Alignment.RIGHT));
      cr();
      dashes(mLineLength);
      cr();
    }

    renderColumns();

    dashes(mLineLength);
    cr();

    saveToDrive();
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
    log("|:|\n" + mPageBuffer.toString());
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

  private PrintManager setMaxLength(int maxLength) {
    printCol().setMaxLength(maxLength);
    return this;
  }

  private PrintManager right() {
    printCol().setAlignment(Alignment.RIGHT);
    return this;
  }

  public PrintManager stretchPct(int pct) {
    printCol().mStretchPct = pct;
    return this;
  }

  public PrintManager shrinkPct(int pct) {
    printCol().mShrinkPct = pct;
    return this;
  }

  private PrintCol printCol() {
    if (mCurrentPrintCol == null)
      mCurrentPrintCol = new PrintCol();
    return mCurrentPrintCol;
  }

  private PrintManager addCol(String debugName) {
    checkState(mCurrentPrintCol != null, "no PrintCol to add for", debugName);
    mCurrentPrintCol.setName(debugName);
    mPrintCols.add(mCurrentPrintCol);
    mCurrentPrintCol = null;
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
    var printCol = mPrintCols.get(mColNumber);
    printCol.add(data);
    mColNumber++;
    return this;
  }

  private PrintManager renderColumns() {
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

    log("maxLineLength:", mMaxLineLength, "widthSum:", widthSum, "targetLineLength:", mTargetLineLength,
        "slack:", slack, "crop:", cropAmount);

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

  private void init() {
    mBuffer = new StringBuilder();
    mPageBuffer = new StringBuilder();
    mPrintCols = arrayList();
    mCurrentPrintCol = null;
  }

  private StringBuilder mBuffer;
  private StringBuilder mPageBuffer;
  private int mMaxLineLength = 110;
  private int mTargetLineLength;
  private String mTitle = "untitled";
  private File mDir;
  private List<PrintCol> mPrintCols;
  private PrintCol mCurrentPrintCol;
  private int mColNumber;
  private int mLineLength;
}
