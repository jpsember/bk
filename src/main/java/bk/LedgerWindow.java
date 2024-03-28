package bk;

import static js.base.Tools.*;

import java.util.List;

import com.googlecode.lanterna.input.KeyStroke;

import bk.gen.Alignment;
import bk.gen.Column;
import js.geometry.MyMath;

public class LedgerWindow implements WindowHandler {

  @Override
  public void paint(JWindow window) {
    mWindow = window;

    todo("we need an ability to paint parts of a window even when the whole window is not invalid?");

    var b = window.clipBounds();

    // Determine the starting offset, to keep the cursor row near the center of the window
    int ledgerRowNumAtTopOfWindow = 0;
    {
      int vis = b.height;
      ledgerRowNumAtTopOfWindow = Math.max(-1, (mCursorRow - vis / 2));
    }

    final int SPACES_BETWEEN_COLUMNS = 2;
    String blockText = null;
    int rows = b.height;
    for (int windowRowNum = 0; windowRowNum < rows; windowRowNum++) {
      int ledgerRowNum = windowRowNum + ledgerRowNumAtTopOfWindow;

      msb.setLength(0);

      int x = 0;
      if (ledgerRowNum == mCursorRow) {
        todo("just plotting >>> for cursor");
        plotString(">>>", x, windowRowNum, Alignment.LEFT, 3);
        x += 4;
      }
      if (ledgerRowNum == -1) {
        // Render the headings
        for (var col : mColumns) {
          plotString(col.name(), x, windowRowNum, col.alignment(), col.width());
          x += col.width() + SPACES_BETWEEN_COLUMNS;
        }
      } else {
        int entNum = ledgerRowNum;
        if (entNum >= mEntries.size()) {
          // Plot a row of grey to indicate we're off the ledger
          if (blockText == null) {
            for (int j = 0; j < b.width; j++)
              msb.append('░');
            blockText = msb.toString();
          }
          plotString(blockText, 0, windowRowNum, Alignment.LEFT, blockText.length());
          continue;
        }

        var entries = mEntries.get(entNum);

        // Render the fields
        var j = INIT_INDEX;
        for (var col : mColumns) {
          j++;
          var data = entries.get(j);
          var text = data.toString();
          if (false && alert("printing really long stuff"))
            text = "abcdefghijklmnopqrstuvwxyz_abcdefghijklmnopqrstuvwxyz_abcdefghijklmnopqrstuvwxyz";
          plotString(text, x, windowRowNum, col.alignment(), col.width());
          x += col.width() + SPACES_BETWEEN_COLUMNS;
        }
      }
    }
  }

  @Override
  public void processKeyStroke(JWindow window, KeyStroke k) {
    mWindow = window;
    //    pr(VERT_SP, "ledger keystroke:", k);

    Integer targetEntry = null;
    int pageSize = window.clipBounds().height;
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
    default:
      break;
    }
    if (targetEntry != null) {
      int sz = mEntries.size();
      if (sz != 0) {
        int t = MyMath.clamp(targetEntry, 0, sz - 1);
        todo("have ability to repaint specific rows");
        mCursorRow = t;
        window.repaint();
      }
    }
  }

  private void plotString(String text, int x, int y, Alignment alignment, int width) {
    var b = mWindow.clipBounds();
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
    mWindow.drawString(x + b.x, y + b.y, width, text);
  }

  public void addColumn(Column column) {
    column = adjustColumn(column);
    mColumns.add(column.build());
  }

  public void addEntry(List<LedgerField> fields) {
    checkArgument(fields.size() == mColumns.size(), "expected", mColumns.size(), "entries, got",
        fields.size());
    mEntries.add(fields);
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

  private List<Column> mColumns = arrayList();
  private List<List<LedgerField>> mEntries = arrayList();
  private StringBuilder msb = new StringBuilder();
  private JWindow mWindow;
  private int mCursorRow;
}
