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
    setBorder(BORDER_ROUNDED);
  }

  public FormWindow addButton(String label, ButtonListener listener) {
    WidgetWindow widget;
    widget = new WidgetWindow().focusRootWindow(this).label(label).button(listener);
    widget.setSize(1);
    addChild(widget);
    return this;
  }

  public FormWindow addMessageLine() {
    checkState(mMessage == null);
    MessageWindow widget;
    widget = new MessageWindow();
    widget.setSize(1);
    addChild(widget);
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
    addChild(w);
    return this;
  }

  public FormWindow fieldWidth(int width) {
    mPendingWidth = width;
    return this;
  }

  public WidgetWindow addField(String label) {
    WidgetWindow widget;
    widget = new WidgetWindow().focusRootWindow(this).label(label).width(mPendingWidth);
    mPendingWidth = WidgetWindow.DEFAULT_WIDTH;
    widget.setSize(1);
    widget.validator(nullTo(mPendingValidator, DEFAULT_VALIDATOR));
    widget.value(mPendingValue);
    mPendingValidator = null;
    mPendingValue = null;
    addChild(widget);
    return widget;
  }

  public FormWindow validator(Validator v) {
    mPendingValidator = v;
    return this;
  }

  public FormWindow value(Object obj) {
    mPendingValue = obj;
    return this;
  }

  private Validator mPendingValidator;
  private Object mPendingValue;
  private MessageWindow mMessage;
  private int mPendingWidth = WidgetWindow.DEFAULT_WIDTH;;
}
