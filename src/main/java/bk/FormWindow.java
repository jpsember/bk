package bk;

import static bk.Util.*;
import static js.base.Tools.*;

/**
 * A container that represents a form
 */
public class FormWindow extends JContainer {

  public FormWindow() {
    todo("incorporate this into WinMgr");
    loadUtil();
    setBorder(BORDER_THIN);
  }

  public FormWindow addField(String label) {
    // Add a widget 
    JWindow widget;
    widget = new WidgetWindow().label(label);
    widget.setSize(1);

    children().add(widget);
    return this;
  }

  @Override
  void render(boolean partial) {
    pr("rendering FormWindow");
    super.render(partial);
  }

}
