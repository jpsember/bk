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

  public FormWindow addButton(String label, ButtonListener listener) {
    WidgetWindow widget;
    widget = new WidgetWindow().focusRootWindow(this).label(label).button(listener);
    widget.setSize(1);
    children().add(widget);
    return this;
  }

  public FormWindow addMessageLine() {
    checkState(mMessage == null);
    MessageWindow widget;
    widget = new MessageWindow();
    widget.setSize(1);
    children().add(widget);
    mMessage = widget;
    return this;
  }

  public FormWindow setMessage(String message) {
    checkState(mMessage != null);
    mMessage.message(message);
    mMessage.repaint();
    return this;
  }

  public FormWindow addVertSpace(int count) {
    var w = new JWindow();
    w.setSize(count);
    children().add(w);
    return this;
  }

  public WidgetWindow addField(String label) {
    WidgetWindow widget;
    widget = new WidgetWindow().focusRootWindow(this).label(label);
    widget.setSize(1);
    widget.validator(nullTo(mPendingValidator, DEFAULT_VALIDATOR));
    mPendingValidator = null;
    children().add(widget);
    return widget;
  }

  public FormWindow validator(Validator v) {
    mPendingValidator = v;
    return this;
  }

  private Validator mPendingValidator;
  private MessageWindow mMessage;
}
