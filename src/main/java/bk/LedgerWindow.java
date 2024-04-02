package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.lanterna.input.KeyStroke;

import bk.gen.Alignment;
import bk.gen.Column;
import js.geometry.IRect;
import js.geometry.MyMath;

public class LedgerWindow extends JWindow implements FocusHandler {

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
            r.clearRow(b.y + windowRowNum, '░');
          }
        } else {

          var ent = mEntries.get(entNum);

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
  public void processKeyStroke(KeyStroke k) {
    if (mLastRenderedClipBounds == null) {
      alert("can't process KeyStroke; window has never been rendered");
      return;
    }

    Integer targetEntry = null;
    int pageSize = mLastRenderedClipBounds.height - 2; // Assume a boundary

    switch (k.getKeyType()) {
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
    case Character: {
      var ch = k.getCharacter();
      // These will get me in trouble
      switch (ch) {
      case VIM_UP_CHAR:
        targetEntry = mCursorRow - 1;
        break;
      case VIM_DOWN_CHAR:
        targetEntry = mCursorRow + 1;
        break;
      }
    }
      break;
    default:
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
    column = adjustColumn(column);
    mColumns.add(column.build());
  }

  public void addEntry(List<LedgerField> fields) {
    addEntry(fields, null);
  }

  public void addEntry(List<LedgerField> fields, Object auxData) {
    checkArgument(fields.size() == mColumns.size(), "expected", mColumns.size(), "entries, got",
        fields.size());
    var ent = new Entry();
    ent.auxData = auxData;
    ent.fields = new ArrayList<>(fields);
    mEntries.add(ent);
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

  private static class Entry {
    Object auxData;
    List<LedgerField> fields;
  }

  private List<Column> mColumns = arrayList();
  private List<Entry> mEntries = arrayList();
  private StringBuilder msb = new StringBuilder();
  private int mCursorRow;
  private IRect mLastRenderedClipBounds;

}
