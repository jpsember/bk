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
import js.geometry.MyMath;

public abstract class LedgerWindow extends JWindow implements FocusHandler {

  public LedgerWindow() {
    r = new ResetState();
    setBorder(BORDER_THICK);
  }

  public void reset() {
    r = new ResetState();
  }

  public boolean isItemMarked(Object auxData) {
    return false;
  }

  public void setHeaderHeight(int height) {
    r.mHeaderHeight = height;
  }

  public void setFooterHeight(int height) {
    r.mFooterHeight = height;
  }

  public int chooseCurrentRow() {
    return 0;
  }

  @Override
  protected String supplyName() {
    return "LedgerWindow";
  }

  public void plotColumnLabels(int y) {
    var rn = Render.SHARED_INSTANCE;
    int i = INIT_INDEX;
    int x = rn.clipBounds().x;
    for (var col : r.mColumns) {
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
    var rn = Render.SHARED_INSTANCE;
    var clip = rn.clipBounds();

    int headerRowTotal = r.mHeaderHeight;
    var footerRowTotal = r.mFooterHeight;
    if (!hasFocus()) {
      footerRowTotal = 0;
    }
    int headerScreenY = clip.y;
    int bodyRowTotal = clip.height - headerRowTotal - footerRowTotal;
    int bodyScreenY = headerScreenY + headerRowTotal;
    var footerScreenY = bodyScreenY + bodyRowTotal;
    r.mLastBodyRowTotal = bodyRowTotal;

    calculateColumnWidths(clip.width);

    plotHeader(headerScreenY, r.mHeaderHeight);
    if (footerRowTotal > 0)
      plotFooter(footerScreenY, footerRowTotal);

    // Determine the starting offset, to keep the cursor row near the center of the window
    int firstLedgerRowNum = Math.max(0, (r.mCursorRow - bodyRowTotal / 2));

    for (int bodyRowIndex = 0; bodyRowIndex < bodyRowTotal; bodyRowIndex++) {
      int rowScreenY = bodyRowIndex + bodyScreenY;
      r.msb.setLength(0);

      int rowNum = bodyRowIndex + firstLedgerRowNum;

      Entry ent = null;
      boolean marked = false;
      if (!(rowNum < 0 || rowNum >= r.mEntries.size())) {
        ent = r.mEntries.get(rowNum);
        marked = isItemMarked(ent.auxData);
      }
      var style = STYLE_NORMAL;
      var hl = hasFocus() && rowNum == r.mCursorRow;
      if (hl)
        style = marked ? STYLE_INVERSE_AND_MARK : STYLE_INVERSE;
      else if (marked)
        style = STYLE_MARKED;

      rn.pushStyle(style);
      rn.clearRow(rowScreenY, ' ');

      if (ent != null) {
        // Render the fields
        int x = clip.x;
        mCurrentColumn = INIT_INDEX;
        for (var col : r.mColumns) {
          mCurrentColumn++;
          var data = ent.fields.get(mCurrentColumn);
          var text = data.toString();
          var cw = mColumnWidths[mCurrentColumn];
          plotString(text, x, rowScreenY, col.alignment(), cw);
          x += cw;
        }
      }
      rn.pop();
    }
  }

  private int mCurrentColumn;
  private int[] mColumnWidths;

  private void calculateColumnWidths(int viewWidth) {
    var db = false && alert("debug");
    if (db)
      pr(VERT_SP, "calc column widths, view width:", viewWidth);
    int nc = r.mColumns.size();
    mColumnWidths = new int[nc];
    int sum = 0;
    int pctSum = 0;
    {
      int i = INIT_INDEX;
      for (var c : r.mColumns) {
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
      for (var c : r.mColumns) {
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

  @Override
  public void processKeyEvent(KeyEvent k) {
    Integer targetEntry = null;
    int pageSize = r.mLastBodyRowTotal;

    boolean resetHint = true;

    switch (k.toString()) {
    case KeyEvent.ARROW_UP:
      targetEntry = r.mCursorRow - 1;
      break;
    case KeyEvent.ARROW_DOWN:
      targetEntry = r.mCursorRow + 1;
      break;
    case ":PageUp":
      targetEntry = r.mCursorRow - pageSize;
      break;
    case ":PageDown":
      targetEntry = r.mCursorRow + pageSize;
      break;
    case ":Home":
      targetEntry = 0;
      break;
    case ":End":
      targetEntry = r.mEntries.size();
      break;
    case ":Q":
      winMgr().quit();
      return;
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
    if (r.mPrepared)
      return;

    if (!r.mColumns.isEmpty()) {
      int extra = 100;
      // If no columns have grow percent, add to last
      for (var c : r.mColumns) {
        if (c.growPct() != 0) {
          extra = 0;
          break;
        }
      }
      if (extra != 0) {
        int j = r.mColumns.size() - 1;
        var c = r.mColumns.get(j);
        c = c.toBuilder().growPct(extra).build();
        r.mColumns.set(j, c);
      }
    }
    r.mPrepared = true;
  }

  public void addColumn(Column column) {
    checkState(!r.mPrepared, "cannot add more columns once rendered");
    if (r.mSep == null) {
      r.mSep = r.mPendingSep;
    }

    if (r.mColumns.size() != 0) {
      r.mColumns.add(r.mSep == 0 ? COLUMN_SEPARATOR_SPACES : COLUMN_SEPARATOR_VERTICAL_BAR);
    }
    column = adjustColumn(column);
    r.mColumns.add(column.build());
  }

  public void clearEntries() {
    r.mEntries.clear();
  }

  public LedgerWindow verticalSeparators() {
    checkState(r.mSep == null);
    r.mPendingSep = 1;
    return this;
  }

  public LedgerWindow spaceSeparators() {
    checkState(r.mSep == null);
    r.mPendingSep = 0;
    return this;
  }

  public LedgerWindow openEntry() {
    checkState(r.mLedgerFieldList == null);
    r.mLedgerFieldList = arrayList();
    return this;
  }

  public LedgerWindow add(LedgerField f) {
    checkState(r.mLedgerFieldList != null);
    if (r.mLedgerFieldList.size() != 0) {
      r.mLedgerFieldList.add(mSep == 0 ? SEPARATOR_FIELD_SPACES : SEPARATOR_FIELD_VERTICAL_BAR);
    }
    r.mLedgerFieldList.add(f);
    return this;
  }

  public LedgerWindow addHint(String sentence) {
    checkState(mLedgerFieldList != null);
    int target = mEntries.size();
    trie().addSentence(sentence, null);
    mHintToRowNumberMap.put(sentence, target);
    return this;
  }

  private Map<String, Integer> mHintToRowNumberMap;

  private Trie mTrie;

  private Trie trie() {
    if (mTrie == null) {
      mTrie = new Trie();
      mHintToRowNumberMap = hashMap();
    }
    return mTrie;
  }

  public LedgerWindow closeEntry(Object auxData) {
    checkState(mLedgerFieldList != null);
    var fields = mLedgerFieldList;
    checkArgument(fields.size() == mColumns.size(), "expected", mColumns.size(), "entries, got",
        fields.size());
    var ent = new Entry();
    ent.auxData = auxData;
    ent.fields = new ArrayList<>(fields);
    mEntries.add(ent);
    mLedgerFieldList = null;
    //    mTriggerStringMap = null;
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
    if (r.mCursorRow >= r.mEntries.size())
      return null;
    return entry(r.mCursorRow);
  }

  public int currentRowIndex() {
    return r.mCursorRow;
  }

  private int indexOfAuxData(Object auxData) {
    int j = INIT_INDEX;
    for (var x : r.mEntries) {
      j++;
      if (x.auxData.equals(auxData))
        return j;
    }
    return -1;
  }

  public int size() {
    return r.mEntries.size();
  }

  public <T> T entry(int index) {
    return (T) r.mEntries.get(index).auxData;
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
    if (r.mCursorRow != newCursor) {
      r.mCursorRow = newCursor;
      repaint();
    }
  }

  private int clampCursor(int cursor) {
    cursor = MyMath.clamp(cursor, 0, Math.max(0, r.mEntries.size() - 1));
    return cursor;
  }

  private static class Entry {
    Object auxData;
    List<LedgerField> fields;
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
    var result = trie().query(prefix);
    return mHintToRowNumberMap.get(result);
  }

  private StringBuilder mHintBuffer = new StringBuilder();
  private long mLastHintKeyTime;

  private static class ResetState {
    // State cleared by reset():

    List<LedgerField> mLedgerFieldList;
    boolean mPrepared;
    List<Column> mColumns = arrayList();
    List<Entry> mEntries = arrayList();
    StringBuilder msb = new StringBuilder();
    int mCursorRow;
    int mLastBodyRowTotal = 10;
    int mPendingSep = 1;
    Integer mSep;
    int mHeaderHeight = 2;
    int mFooterHeight = 0;
  }

  private ResetState r;

}
