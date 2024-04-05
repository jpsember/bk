package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.lanterna.Symbols;

import bk.gen.Alignment;
import bk.gen.Column;
import bk.gen.Datatype;
import js.geometry.IRect;
import js.geometry.MyMath;

public class LedgerWindow extends JWindow implements FocusHandler {

  public LedgerWindow() {
    setBorder(BORDER_THIN);
    todo("!allow one or more columns to be expandable by percentages to use available space");
    todo("Have option for horizontal line separating header from data");
  }

  @Override
  protected String supplyName() {
    return "LedgerWindow";
  }

  public boolean includesHeaderFields() {
    return true;
  }

  @Override
  public void paint() {
    var r = Render.SHARED_INSTANCE;

    var b = r.clipBounds();
    mLastRenderedClipBounds = b;
    if (r.partial()) {
      var rn = MyMath.random();
      r.pushStyle(STYLE_INVERSE);
      r.clearRow(b.y + rn.nextInt(20), (char) (rn.nextInt(26) + 'A'));
      r.pop();
      return;
    }

    // Determine the starting offset, to keep the cursor row near the center of the window
    int ledgerRowNumAtTopOfWindow = 0;
    {
      int vis = b.height;
      ledgerRowNumAtTopOfWindow = Math.max(includesHeaderFields() ? -1 : 0, (mCursorRow - vis / 2));
    }

    final int SPACES_BETWEEN_COLUMNS = 2;
    int rows = b.height;
    for (int windowRowNum = 0; windowRowNum < rows; windowRowNum++) {
      int ledgerRowNum = windowRowNum + ledgerRowNumAtTopOfWindow;
      msb.setLength(0);

      int x = 0;

      var hl = hasFocus() && ledgerRowNum == mCursorRow;
      r.pushStyle(hl ? STYLE_INVERSE : STYLE_NORMAL);
      if (hl)
        r.clearRow(b.y + windowRowNum, ' ');
      if (includesHeaderFields() && ledgerRowNum == -1) {
        // Render the headings
        for (var col : mColumns) {
          plotString(col.name(), x, windowRowNum, col.alignment(), col.width());
          x += col.width() + SPACES_BETWEEN_COLUMNS;
        }
      } else {
        int entNum = ledgerRowNum;
        if (entNum >= mEntries.size()) {
          if (includesHeaderFields()) {
            // Plot a row of grey to indicate we're off the ledger
            if (false)
              r.clearRow(b.y + windowRowNum, '░');
          }
        } else {

          var ent = mEntries.get(entNum);
          //pr("rendering entry number:",entNum, ent.fields);
          // Render the fields
          var j = INIT_INDEX;
          for (var col : mColumns) {
            j++;
            var data = ent.fields.get(j);
            var text = data.toString();
            plotString(text, x, windowRowNum, col.alignment(), col.width());
            x += col.width() + SPACES_BETWEEN_COLUMNS;
          }
        }
      }
      r.pop();
    }
  }

  @Override
  public void processKeyEvent(KeyEvent k) {
    if (mLastRenderedClipBounds == null) {
      alert("can't process KeyStroke; window has never been rendered");
      return;
    }

    Integer targetEntry = null;
    int pageSize = mLastRenderedClipBounds.height - 2; // Assume a boundary

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
      switch (k.toString()) {
      case KeyEvent.VIEW_TRANSACTIONS:
      case KeyEvent.VIEW_ACCOUNTS:
        switchToView(k);
        break;
      }
      break;
    }

    if (targetEntry != null)

    {
      int sz = mEntries.size();
      if (sz != 0) {
        int t = MyMath.clamp(targetEntry, 0, sz - 1);
        mCursorRow = t;
        repaint();
      }
    } else if (false && alert("experiment with partial repaint")) {
      pr("triggering partial repaint");
      repaintPartial();
    }
  }

  private void plotString(String text, int x, int y, Alignment alignment, int width) {
    if (width < 0)
      width = text.length();
    var r = Render.SHARED_INSTANCE;
    var b = r.clipBounds();
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
    r.drawString(x + b.x, y + b.y, width, text);
  }

  public void addColumn(Column column) {
    if (mSep == null) {
      mSep = mPendingSep;
    }

    if (mColumns.size() != 0) {
      mColumns.add(mSep == 0 ? SPACE_SEP : VERT_SEP);
    }
    column = adjustColumn(column);
    mColumns.add(column.build());
  }

  public void clearEntries() {
    mEntries.clear();
  }

  //  public void addEntry(List<LedgerField> fields) {
  //    addEntry(fields, null);
  //  }

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
    checkState(mLedgerFieldList == null);
    mLedgerFieldList = arrayList();
    return this;
  }

  public LedgerWindow add(LedgerField f) {
    checkState(mLedgerFieldList != null);
    if (mLedgerFieldList.size() != 0) {
      mLedgerFieldList.add(mSep == 0 ? SPACE_SEP_FLD : VERT_SEP_FLD);
    }
    mLedgerFieldList.add(f);
    return this;
  }

  public LedgerWindow closeEntry(Object auxData) {
    checkState(mLedgerFieldList != null);
    var fields = mLedgerFieldList;
    //   addEntry(v, t);
    //  public void addEntry(List<LedgerField> fields, Object auxData) {
    checkArgument(fields.size() == mColumns.size(), "expected", mColumns.size(), "entries, got",
        fields.size());
    var ent = new Entry();
    ent.auxData = auxData;
    ent.fields = new ArrayList<>(fields);
    //pr("added entry #",mEntries.size(),"fields:",ent.fields);
    mEntries.add(ent);
    mLedgerFieldList = null;
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
    var ent = mEntries.get(mCursorRow);
    return (T) ent.auxData;
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

  public <T> void setCurrentRow(T auxData) {
    boolean db = false && alert("verbosity");
    if (db)
      pr("setCurrentRow to:", INDENT, auxData);
    int newCursor = 0;
    if (auxData != null) {
      int pos = indexOfAuxData(auxData);
      if (db)
        pr("position was:", pos);
      if (pos >= 0)
        newCursor = pos;
    }
    if (mCursorRow != newCursor) {
      mCursorRow = newCursor;
      if (db)
        pr("set cursor row to:", mCursorRow);
      repaint();
    }
  }

  private static class Entry {
    Object auxData;
    List<LedgerField> fields;
  }

  private static final Column VERT_SEP = Column.newBuilder().datatype(Datatype.TEXT).name("").width(1)
      .build();
  private static final Column SPACE_SEP = Column.newBuilder().datatype(Datatype.TEXT).name("").width(1)
      .build();
  private static final LedgerField VERT_SEP_FLD = new LedgerField() {
    @Override
    public String toString() {
      return SIN;
    }

    private String SIN = Character.toString(Symbols.SINGLE_LINE_VERTICAL);
  };

  private static final LedgerField SPACE_SEP_FLD = new LedgerField() {
    @Override
    public String toString() {
      return " ";
    }
  };

  private List<Column> mColumns = arrayList();
  private List<Entry> mEntries = arrayList();
  private StringBuilder msb = new StringBuilder();
  private int mCursorRow;
  private IRect mLastRenderedClipBounds;
  private int mPendingSep = 1;
  private Integer mSep;
}
