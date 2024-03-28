package bk;

import static js.base.Tools.*;

import java.util.List;

import bk.gen.Alignment;
import bk.gen.Column;

public class LedgerWindow implements WindowHandler {

  @Override
  public void paint(JWindow window) {
    mWindow = window;

    pr("painting ledger window; # columns:", mColumns.size());
    todo("we need an ability to paint parts of a window even when the whole window is not invalid?");

    final int SPACES_BETWEEN_COLUMNS = 2;
    var b = window.bounds();
    int rows = b.height;
    for (int i = 0; i < rows; i++) {
      msb.setLength(0);

      int x = 0;
      if (i == 0) {
        // Render the headings
        for (var col : mColumns) {
          plotString(col.name(), x, i, col.alignment(), col.width());
          x += col.width() + SPACES_BETWEEN_COLUMNS;
        }
      } else {
        int entNum = i - 1;
        if (entNum >= mEntries.size())
          continue;

        var entries = mEntries.get(entNum);

        // Render the fields
        var j = INIT_INDEX;
        for (var col : mColumns) {
          j++;
          var data = entries.get(j);
          pr("plot column, width:", col.width(), "x:", x, "y:", i, "data string:", data.toString());
          plotString(data, x, i, col.alignment(), col.width());
          x += col.width() + SPACES_BETWEEN_COLUMNS;
        }
      }
    }
  }

  private void plotString(Object data, int x, int y, Alignment alignment, int width) {

    var text = data.toString();
    var b = mWindow.bounds();

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
    todo("this is unnecessary?");
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
}
