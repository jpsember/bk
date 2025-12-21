package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.io.File;
import java.util.List;

import bk.gen.Account;
import bk.gen.Alignment;
import js.base.BaseObject;
import js.base.BasePrinter;
import js.file.Files;

public class PrintManager extends BaseObject {

  public static final PrintManager SHARED_INSTANCE = new PrintManager();

  public PrintManager toPDF(boolean pdf) {
    mPDF = pdf;
    return this;
  }

  public void printExpandedLedger(Account a) {
    auxPrintLedger(a, true);
  }

  public void printLedger(Account a) {
    auxPrintLedger(a, false);
  }

  public void auxPrintLedger(Account a, boolean expanded) {
    //    alertVerbose();
    init();

    var ts = storage().readTransactionsForAccount(a.number());

    setMaxLength(CHARS_DATE).addCol("Date");

    right().setMaxLength(CHARS_CURRENCY).addCol("Debit");
    right().setMaxLength(CHARS_CURRENCY).addCol("Credit");
    if (expanded) {
      setMaxLength(CHARS_ACCOUNT_NUMBER_AND_NAME).stretchPct(30).shrinkPct(20).addCol("Account");
    } else {
      setMaxLength(CHARS_ACCOUNT_NAME).stretchPct(30).shrinkPct(25).addCol("Account");
    }
    right().setMaxLength(CHARS_CURRENCY).addCol("Balance");
    var SMALL_DESCR_LEN = 24;

    if (expanded)
      setMaxLength(CHARS_TRANSACTION_DESCRIPTION).stretchPct(100).shrinkPct(50).addCol("Memo");
    else
      setMaxLength(SMALL_DESCR_LEN).shrinkPct(0).stretchPct(0).addCol("Memo");

    var date = formatDate(epochSecondsToday());

    long currBal = 0;
    for (var t : ts) {
      var other = otherAccount(t, a.number()).number();
      var signedAmount = signedAmount(t, a.number());
      currBal += signedAmount;

      beginLine();
      col(formatDate(t.date()));
      var curStr = formatCurrency(t.amount());
      if (other == t.credit())
        col(curStr).col("");
      else
        col("").col(curStr);

      var ac = otherAccount(t, a.number());
      if (expanded) {
        col(accountNumberWithNameString(ac));
      } else {
        col(ac.name());
      }

      col(formatCurrency(currBal));
      if (expanded)
        col(t.description());
      else {
        var note = t.description();
        // If the note is too long for the 'small' description length, use a footnote instead
        if (note.length() > SMALL_DESCR_LEN)
          note = addFootnoteIfNonEmpty(note);
        col(note);
      }
      endLine();
    }

    determineColumnWidths();
    setTitle(a.number(), a.name(), date);

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
    renderColumnHeadings();
    dashes(mLineLength);
    cr();
    renderColumns();

    dashes(mLineLength);
    cr();

    renderFootnotes();

    saveToDrive();
  }

  private String addFootnoteIfNonEmpty(String note) {
    if (note.isEmpty())
      return note;
    if (false)
      note = randomText(150, false);
    mFootnotes.add(note);
    return "Note " + mFootnotes.size();
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

  private PrintManager cr() {
    var str = mBuffer.toString();
    mBuffer.setLength(0);
    str = trimToWidth(str, mLineLength);
    mPageBuffer.append(str);
    log(">>> ", str);
    mPageBuffer.append('\n');
    return this;
  }

  private PrintManager dashes(int count) {
    for (int i = 0; i < count; i++)
      mBuffer.append('-');
    return this;
  }

  public PrintManager saveToDrive() {
    var filename = checkNonEmpty(mTitle, "no title defined");
    filename = Files.addExtension(filename, mPDF ? "pdf" : Files.EXT_TEXT);

    if (mDir == null)
      toDirectory(Files.getDesktopDirectory());
    var f = new File(mDir, filename);
    log("saving to:", f);
    var content = mPageBuffer.toString();

    if (mPDF) {
      var p = new PDFWriter();
      p.target(f);
      p.content(content);
      p.close();
    } else {
      Files.S.writeString(f, content);
    }
    log("\n" + content);
    setMessageDuration(20);
    setFooterMessage("Printed", f);
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

  private static final String COL_SEP_STRING = " | ";

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
          sb.append(COL_SEP_STRING);
        str = trimToWidth(str, pc.mLengthRequired);
        str = justify(str, pc.mLengthRequired, pc.mAlignment);
        sb.append(str);
      }
      cr();
    }

    return this;
  }

  private void renderColumnHeadings() {
    var sb = mBuffer;
    sb.setLength(0);
    int j = INIT_INDEX;
    for (var pc : mPrintCols) {
      j++;
      var name = pc.name();
      if (name.startsWith("!"))
        name = "";
      if (j != 0)
        sb.append(COL_SEP_STRING);
      name = trimToWidth(name, pc.mLengthRequired);
      name = justify(name, pc.mLengthRequired, pc.mAlignment);
      sb.append(name);
    }
    cr();
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
    //alertVerbose();
    log("determine column widths");
    todo("add support for minimum width in chars to prevent shrinkage too far");
    int widthSum = 0;
    {
      int i = INIT_INDEX;
      for (var c : mPrintCols) {
        i++;
        if (i != 0)
          widthSum += COL_SEP_STRING.length();
        widthSum += c.requiredLength();
        log("col", i, "width", c.requiredLength(), "sum", widthSum);
      }
    }

    if (mTargetLineLength == 0)
      mTargetLineLength = Math.min(widthSum, mMaxLineLength);

    log("target line length:", mTargetLineLength);

    int maxExpandChars = Math.max(0, mTargetLineLength - widthSum);
    int maxCropChars = Math.max(0, widthSum - mMaxLineLength);
    log("exp:", maxExpandChars, "crop:", maxCropChars);

    int remain = maxExpandChars;
    boolean expanding;
    if (maxCropChars > 0) {
      remain = maxCropChars;
      expanding = false;
    } else
      expanding = true;

    var sign = expanding ? 1 : -1;

    if (remain > 0) {
      var origRemain = remain;
      float[] f = new float[mPrintCols.size()];
      float changeTotal = 0;
      {
        int i = INIT_INDEX;
        for (var v : mPrintCols) {
          i++;
          var x = (expanding ? v.mStretchPct : v.mShrinkPct) * 100f;
          f[i] = x;
          changeTotal += x;
        }
      }
      log("change factors:", f);
      {
        int biggestFactor = -1;
        int i = INIT_INDEX;
        for (var v : mPrintCols) {
          i++;
          var x = f[i];
          if (biggestFactor < 0 || x > f[biggestFactor])
            biggestFactor = i;
          int ourChars = Math.round((x / changeTotal) * origRemain);
          ourChars = Math.min(ourChars, remain);
          if (ourChars != 0) {
            var amt = ourChars * sign;
            log("adjusting field", v.name(), "by", amt);
            v.adjustWidth(amt);
            remain -= ourChars;
          }
        }
        if (remain > 0 && biggestFactor >= 0) {
          var v = mPrintCols.get(biggestFactor);
          int amt = remain * sign;
          log("final adjustment of field", v.name(), "by", amt);
          v.adjustWidth(amt);
          remain = 0;
        }
      }
      widthSum += (origRemain - remain) * sign;
    }
    mLineLength = widthSum;
    log("modified line length now:", mLineLength);
  }

  private void init() {
    mBuffer = new StringBuilder();
    mPageBuffer = new StringBuilder();
    mPrintCols = arrayList();
    mCurrentPrintCol = null;
    mFootnotes = arrayList();
  }

  private void renderFootnotes() {
    int i = INIT_INDEX;
    for (var footNote : mFootnotes) {
      i++;
      var sb = mBuffer;
      sb.setLength(0);
      sb.append(1 + i);
      sb.append(". ");
      while (!footNote.isEmpty()) {
        var prefix = extractLine(footNote, mLineLength - sb.length());
        footNote = footNote.substring(prefix.length()).trim();
        sb.append(prefix);
        cr();
        sb.append("    ");
      }
    }
  }

  private StringBuilder mBuffer;
  private StringBuilder mPageBuffer;
  private int mMaxLineLength = 108;
  private int mTargetLineLength;
  private String mTitle = "untitled";
  private File mDir;
  private List<PrintCol> mPrintCols;
  private PrintCol mCurrentPrintCol;
  private int mColNumber;
  private int mLineLength;
   private List<String> mFootnotes;
  private boolean mPDF = true;
}
