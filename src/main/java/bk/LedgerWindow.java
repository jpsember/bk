package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.input.KeyType;

import bk.gen.Alignment;
import bk.gen.Column;
import bk.gen.Datatype;
import js.base.DateTimeTools;
import js.base.Pair;
import js.geometry.MyMath;

public abstract class LedgerWindow extends JWindow implements FocusHandler {

  public LedgerWindow() {
    setBorder(BORDER_THICK);
  }

  public void setHeaderHeight(int height) {
    mHeaderHeight = height;
  }

  public void setFooterHeight(int height) {
    mFooterHeight = height;
  }

  public int chooseCurrentRow() {
    return 0;
  }

  public void discardCachedAccountInfo() {
    mTriggerStringMap = null;
  }

  @Override
  protected String supplyName() {
    return "LedgerWindow";
  }

  public void plotColumnLabels(int y) {
    var r = Render.SHARED_INSTANCE;
    int i = INIT_INDEX;
    int x = r.clipBounds().x;
    for (var col : mColumns) {
      i++;
      var cw = mColumnWidths[i];
      plotString(col.name(), x, y, col.alignment(), cw);
      x += cw;
    }
  }

  public void plotHeader(int y, int headerHeight) {
    if (headerHeight >= 2) {
      plotColumnLabels(y + headerHeight - 2);
      plotHorzLine(y + headerHeight - 1);
    }
  }

  public void plotFooter(int y, int height) {
    if (height >= 2) {
      plotHorzLine(y);
      y++;
      height--;
    }
    plotFooterContent(y, height);
  }

  public void plotFooterContent(int y, int height) {
    var r = Render.SHARED_INSTANCE;
    var b = r.clipBounds();
    int x = b.x;
    for (int i = 0; i < height; i++)
      plotString("***plotFooterContent unimplemented***", x, y);
  }

  @Override
  public void paint() {
    prepareToRender();
    var r = Render.SHARED_INSTANCE;
    var clip = r.clipBounds();

    int headerRowTotal = mHeaderHeight;
    var footerRowTotal = mFooterHeight;
    if (!hasFocus()) {
      footerRowTotal = 0;
    }
    int headerScreenY = clip.y;
    int bodyRowTotal = clip.height - headerRowTotal - footerRowTotal;
    int bodyScreenY = headerScreenY + headerRowTotal;
    var footerScreenY = bodyScreenY + bodyRowTotal;
    mLastBodyRowTotal = bodyRowTotal;

    calculateColumnWidths(clip.width);

    plotHeader(headerScreenY, mHeaderHeight);
    if (footerRowTotal > 0)
      plotFooter(footerScreenY, footerRowTotal);

    // Determine the starting offset, to keep the cursor row near the center of the window
    int firstLedgerRowNum = Math.max(0, (mCursorRow - bodyRowTotal / 2));

    for (int bodyRowIndex = 0; bodyRowIndex < bodyRowTotal; bodyRowIndex++) {
      int rowScreenY = bodyRowIndex + bodyScreenY;
      msb.setLength(0);

      int rowNum = bodyRowIndex + firstLedgerRowNum;

      Entry ent = null;
      if (!(rowNum < 0 || rowNum >= mEntries.size())) {
        ent = mEntries.get(rowNum);
      }
      var style = STYLE_NORMAL;
      var hl = hasFocus() && rowNum == mCursorRow;
      if (hl)
        style = ent.marked ? STYLE_INVERSE_AND_MARK : STYLE_INVERSE;
      else if (ent != null && ent.marked)
        style = STYLE_MARKED;

      r.pushStyle(style);
      r.clearRow(rowScreenY, ' ');

      if (ent != null) {
        // Render the fields
        int x = clip.x;
        mCurrentColumn = INIT_INDEX;
        for (var col : mColumns) {
          mCurrentColumn++;
          var data = ent.fields.get(mCurrentColumn);
          var text = data.toString();
          var cw = mColumnWidths[mCurrentColumn];
          plotString(text, x, rowScreenY, col.alignment(), cw);
          x += cw;
        }
      }
      r.pop();
    }
  }

  private void calculateColumnWidths(int viewWidth) {
    var db = false && alert("debug");
    if (db)
      pr(VERT_SP, "calc column widths, view width:", viewWidth);
    int nc = mColumns.size();
    mColumnWidths = new int[nc];
    int sum = 0;
    int pctSum = 0;
    {
      int i = INIT_INDEX;
      for (var c : mColumns) {
        i++;
        var w = c.width();
        pctSum += c.growPct();
        if (db)
          pr("...", i, "grow", c.growPct());
        mColumnWidths[i] = w;
        sum += w;
      }
    }

    var extra = viewWidth - sum;
    if (db)
      pr("extra chars:", extra, "pctsum:", pctSum);
    if (extra > 0 && pctSum != 0) {
      var extraRemain = extra;
      int i = INIT_INDEX;
      for (var c : mColumns) {
        i++;
        int thisExtra = Math.round((c.growPct() * (float) extra) / pctSum);
        thisExtra = Math.min(thisExtra, extraRemain);
        mColumnWidths[i] += thisExtra;
        extraRemain -= thisExtra;
      }
    }
    if (db)
      pr("column widths:", mColumnWidths);
  }

  private int mCurrentColumn;
  private int[] mColumnWidths;

  @Override
  public void processKeyEvent(KeyEvent k) {
    Integer targetEntry = null;
    int pageSize = mLastBodyRowTotal;

    boolean resetHint = true;

    switch (k.keyType()) {
    case ArrowUp:
      targetEntry = mCursorRow - 1;
      break;
    case ArrowDown:
      targetEntry = mCursorRow + 1;
      break;
    case PageUp:
      targetEntry = mCursorRow - pageSize;
      break;
    case PageDown:
      targetEntry = mCursorRow + pageSize;
      break;
    case Home:
      targetEntry = 0;
      break;
    case End:
      targetEntry = mEntries.size();
      break;
    default:
      if (processHelper(k)) {
        resetHint = false;
      }
      break;
    }

    if (resetHint)
      resetHintCursor();

    if (targetEntry != null) {
      setCurrentRowIndex(targetEntry);
    }
  }

  protected void plotString(String text, int x, int y) {
    plotString(text, x, y, Alignment.LEFT, -1);
  }

  protected void plotString(String text, int x, int y, Alignment alignment, int width) {
    if (width < 0)
      width = text.length();
    var r = Render.SHARED_INSTANCE;
    var diff = width - text.length();
    if (diff > 0) {
      switch (alignment) {
      case CENTER:
        x += diff >> 1;
        break;
      case RIGHT:
        x += diff;
        break;
      default:
        break;
      }
    }
    r.drawString(x, y, width, text);
  }

  private void prepareToRender() {
    if (mPrepared)
      return;

    if (!mColumns.isEmpty()) {
      int extra = 100;
      // If no columns have grow percent, add to last
      for (var c : mColumns) {
        if (c.growPct() != 0) {
          extra = 0;
          break;
        }
      }
      if (extra != 0) {
        int j = mColumns.size() - 1;
        var c = mColumns.get(j);
        c = c.toBuilder().growPct(extra).build();
        mColumns.set(j, c);
      }
    }
    mPrepared = true;
  }

  private boolean mPrepared;

  public void addColumn(Column column) {
    checkState(!mPrepared, "cannot add more columns once rendered");
    if (mSep == null) {
      mSep = mPendingSep;
    }

    if (mColumns.size() != 0) {
      mColumns.add(mSep == 0 ? COLUMN_SEPARATOR_SPACES : COLUMN_SEPARATOR_VERTICAL_BAR);
    }
    column = adjustColumn(column);
    mColumns.add(column.build());
  }

  public void clearEntries() {
    mEntries.clear();
  }

  private List<LedgerField> mLedgerFieldList;

  public LedgerWindow verticalSeparators() {
    checkState(mSep == null);
    mPendingSep = 1;
    return this;
  }

  public LedgerWindow spaceSeparators() {
    checkState(mSep == null);
    mPendingSep = 0;
    return this;
  }

  public LedgerWindow openEntry() {
    discardCachedAccountInfo();
    checkState(mLedgerFieldList == null);
    mLedgerFieldList = arrayList();
    mPendingMarked = false;
    return this;
  }

  public LedgerWindow add(LedgerField f) {
    checkState(mLedgerFieldList != null);
    if (mLedgerFieldList.size() != 0) {
      mLedgerFieldList.add(mSep == 0 ? SEPARATOR_FIELD_SPACES : SEPARATOR_FIELD_VERTICAL_BAR);
    }
    mLedgerFieldList.add(f);
    return this;
  }

  public LedgerWindow marked(boolean f) {
    checkState(mLedgerFieldList != null);
    mPendingMarked = f;
    return this;
  }

  public LedgerWindow closeEntry(Object auxData) {
    checkState(mLedgerFieldList != null);
    var fields = mLedgerFieldList;
    checkArgument(fields.size() == mColumns.size(), "expected", mColumns.size(), "entries, got",
        fields.size());
    var ent = new Entry();
    ent.auxData = auxData;
    ent.fields = new ArrayList<>(fields);
    ent.marked = mPendingMarked;
    mEntries.add(ent);
    mLedgerFieldList = null;
    mTriggerStringMap = null;
    return this;
  }

  private static Column adjustColumn(Column c) {
    var b = c.build().toBuilder();
    if (b.width() == 0) {
      switch (b.datatype()) {
      case ACCOUNT_NUMBER:
        b.width(4);
        break;
      case CURRENCY:
        b.alignment(Alignment.RIGHT);
        b.width(12);
        break;
      case DATE:
        b.width(10);
        break;
      case TEXT:
        b.width(25);
        break;
      }
    }
    return b;
  }

  public <T> T getCurrentRow() {
    if (mCursorRow >= mEntries.size())
      return null;
    return entry(mCursorRow);
  }

  private int indexOfAuxData(Object auxData) {
    int j = INIT_INDEX;
    for (var x : mEntries) {
      j++;
      if (x.auxData.equals(auxData))
        return j;
    }
    return -1;
  }

  public int size() {
    return mEntries.size();
  }

  public <T> T entry(int index) {
    return (T) mEntries.get(index).auxData;
  }

  public <T> void setCurrentRow(T auxData) {
    int newCursor = -1;
    if (auxData != null) {
      newCursor = indexOfAuxData(auxData);
    }
    if (newCursor < 0)
      newCursor = chooseCurrentRow();
    setCurrentRowIndex(newCursor);
  }

  public void setCurrentRowIndex(int newCursor) {
    newCursor = clampCursor(newCursor);
    if (mCursorRow != newCursor) {
      mCursorRow = newCursor;
      repaint();
    }
  }

  private int clampCursor(int cursor) {
    cursor = MyMath.clamp(cursor, 0, Math.max(0, mEntries.size() - 1));
    return cursor;
  }

  private static class Entry {
    Object auxData;
    List<LedgerField> fields;
    boolean marked;
  }

  private static final Column COLUMN_SEPARATOR_VERTICAL_BAR = Column.newBuilder().datatype(Datatype.TEXT)
      .name("").width(3).build();
  private static final Column COLUMN_SEPARATOR_SPACES = Column.newBuilder().datatype(Datatype.TEXT).name("")
      .width(2).build();

  private static final LedgerField SEPARATOR_FIELD_VERTICAL_BAR = new LedgerField() {
    @Override
    public String toString() {
      return SIN;
    }

    private final String SIN = " " + Symbols.SINGLE_LINE_VERTICAL + " ";
  };

  private static final LedgerField SEPARATOR_FIELD_SPACES = new LedgerField() {
    @Override
    public String toString() {
      return "  ";
    }
  };

  private Map<String, Pair<Integer, Integer>> helperTriggers() {
    if (mTriggerStringMap == null) {
      mTriggerStringMap = hashMap();
      var m = mTriggerStringMap;
      for (int pass = 0; pass < 2; pass++) {
        int rowIndex = INIT_INDEX;
        for (var entry : mEntries) {
          rowIndex++;
          for (var f : entry.fields) {
            var s = f.toString().trim().toLowerCase();
            var words = split(s, ' ');
            int maxLen = (pass == 0) ? 1 : words.size();
            for (int wordIndex = 0; wordIndex < maxLen; wordIndex++) {
              var prefix = words.get(wordIndex);
              if (m.containsKey(prefix))
                continue;
              m.put(prefix, pair(rowIndex, wordIndex));
            }
          }
        }
      }
    }
    return mTriggerStringMap;
  }

  private List<Column> mColumns = arrayList();
  private List<Entry> mEntries = arrayList();
  private StringBuilder msb = new StringBuilder();
  private int mCursorRow;
  private int mLastBodyRowTotal = 10;
  private int mPendingSep = 1;
  private Integer mSep;

  // ------------------------------------------------------------------
  // Hint
  // ------------------------------------------------------------------

  private void resetHintCursor() {
    mHintBuffer.setLength(0);
  }

  private boolean processHelper(KeyEvent event) {
    if (event.keyType() == KeyType.Character && !event.hasCtrlOrAlt() && event.getCharacter() <= 127) {
      var ts = System.currentTimeMillis();
      if (ts - mLastHintKeyTime > DateTimeTools.MILLISECONDS(750))
        resetHintCursor();
      mLastHintKeyTime = ts;

      char ch = event.getCharacter();
      mHintBuffer.append(ch);
      log("hint is now:", mHintBuffer);

      var helperIndex = determineHelperValue(mHintBuffer.toString());
      if (helperIndex != null) {
        setCurrentRowIndex(helperIndex);
      }
      return true;
    }
    return false;
  }

  private Integer determineHelperValue(String prefix) {
    // alertVerbose();
    Integer bestResult = null;
    int bestWordPosition = 0;

    if (prefix.length() != 0) {
      prefix = prefix.toLowerCase();
      var triggerMap = helperTriggers();
      log("determineHelperValue for prefix:", quote(prefix), "triggers:", INDENT, triggerMap);
      String shortestMatch = null;
      for (var mapEntry : triggerMap.entrySet()) {
        var key = mapEntry.getKey();
        if (key.startsWith(prefix)) {
          var val = mapEntry.getValue();
          if (bestResult != null) {
            if (val.second > 0 && bestWordPosition == 0)
              continue;
            if (shortestMatch.length() <= key.length())
              continue;
          }
          shortestMatch = key;
          bestResult = val.first;
          bestWordPosition = val.second;
          log("...new best match:", key, bestResult);

        }
      }
    }
    log("...result:", bestResult);
    return bestResult;
  }

  private StringBuilder mHintBuffer = new StringBuilder();
  private Map<String, Pair<Integer, Integer>> mTriggerStringMap;
  private boolean mPendingMarked;
  private long mLastHintKeyTime;
  private int mHeaderHeight = 2;
  private int mFooterHeight = 0;
}
