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
    setBorder(BORDER_THIN);
    //    addColumn(Column.newBuilder().name("Label").datatype(Datatype.TEXT).width(14).alignment(Alignment.RIGHT));
    //    addColumn(Column.newBuilder().name("Value").datatype(Datatype.TEXT).width(50));
  }

  public FormWindow addField(String label) {

    // Add a widget 
    var widget = new WidgetWindow().label(label);
    widget.setSize(30);

    //    // Add a horizontal container for this entry
    //    var c = new JContainer();
    //    c.mSizeExpr = 1;
    //    c.mHorzFlag = true;
    //    {
    //      var w = new JWindow() {
    //        @Override
    //        public void paint() {
    //          var r = Render.SHARED_INSTANCE;
    //          var b = r.clipBounds();
    //          var s = name;
    //          pr("rendering label, bounds:", b, "name:", s);
    //          r.drawString(b.endX() - s.length(), b.y, s.length(), s);
    //        }
    //      };
    //      w.mSizeExpr = 50;
    //      c.children().add(w);
    //    }
    //    {
    //      widget.mSizeExpr = 50;
    //      c.children().add(widget);
    //    }
    children().add(widget);
    return this;
  }

  @Override
  void render(boolean partial) {
    pr("rendering FormWindow");
    super.render(partial);
  }

  private static class WidgetEntry {
    String name;
    WidgetWindow widget;
  }

  private List<WidgetEntry> mEntries = arrayList();

}
