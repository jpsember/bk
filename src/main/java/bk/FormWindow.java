package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.List;

/**
 * A container that represents a form
 */
public class FormWindow extends JContainer implements FocusHandler {

  public FormWindow() {
    todo("incorporate this into WinMgr");

    todo("highlight only the value field of the current line");
    todo("have cursor keys skip empty fields");
    todo("have blinking cursor on active field somehow");
    loadUtil();
    //    addColumn(Column.newBuilder().name("Label").datatype(Datatype.TEXT).width(14).alignment(Alignment.RIGHT));
    //    addColumn(Column.newBuilder().name("Value").datatype(Datatype.TEXT).width(50));
  }

  public FormWindow addField(String name, WidgetWindow widget) {
    var e = new WidgetEntry();
    e.name = name;
    e.widget = widget;

    mEntries.add(e);

    // Add a horizontal container for this entry
    var c = new JContainer();
    c.mSizeExpr = 1;
    c.mHorzFlag = true;
    {
      var w = new JWindow() {
        @Override
        public void paint() {
          var r = Render.SHARED_INSTANCE;
          var b = r.clipBounds();
          var s = name;
          pr("rendering label, bounds:", b, "name:", s);
          r.drawString(b.endX() - s.length(), b.y, s.length(), s);
        }
      };
      w.mSizeExpr = 50;
      c.children().add(w);
    }
    {
      widget.mSizeExpr = 50;
      c.children().add(widget);
    }
    children().add(c);
    return this;
  }

  private static class WidgetEntry {
    String name;
    WidgetWindow widget;
  }

  //  
  //  @Override
  //  public void layout() {
  //     // Construct a horizontal container for each field
  //    
  //  }
  private List<WidgetEntry> mEntries = arrayList();
}
