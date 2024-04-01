package bk;

import static js.base.Tools.*;

import java.util.List;

import js.base.BaseObject;
import js.geometry.MyMath;
import static bk.Util.*;

public class FocusManager extends BaseObject {

  public static final FocusManager SHARED_INSTANCE;

  private FocusManager() {
  }

  public FocusHandler focus() {
    return mFocus;
  }

  void update() {
    if (focus() != FOCUS_NONE)
      return;
    var lst = handlers(null);
    if (lst.isEmpty())
      return;
    set(lst.get(0));
  }

  public void set(FocusHandler h) {
    h = nullTo(h, FOCUS_NONE);
    if (h == mFocus)
      return;
    if (mFocus != null) {
      winMgr().hideCursor();
      mFocus.loseFocus();
      mFocus.repaint();
    }
    mFocus = h;
    h.gainFocus();
    h.repaint();
  }

  public void move(JWindow rootWindowOrNull, int amount) {
    var focusList = handlers(rootWindowOrNull);
    int slot = focusList.indexOf(mFocus);
    switch (amount) {
    case -1:
    case 1:
      slot = MyMath.myMod(slot + amount, focusList.size());
      break;
    default:
      badArg("unhandled moveFocus arg", amount);
    }
    set(focusList.get(slot));
  }

  public final List<FocusHandler> handlers(JWindow topLevelWindowOrNull) {
    var w = nullTo(topLevelWindowOrNull, winMgr().topLevelContainer());
    List<FocusHandler> out = arrayList();
    auxFocusList(out, w);
    return out;
  }

  private void auxFocusList(List<FocusHandler> list, JWindow window) {
    if (window.hidden())
      return;
    if (window instanceof FocusHandler) {
      list.add((FocusHandler) window);
    }

    for (var c : window.children())
      auxFocusList(list, c);
  }

  private FocusHandler mFocus = FOCUS_NONE;

  public static final FocusHandler FOCUS_NONE = new FocusHandler() {
  };

  static {
    SHARED_INSTANCE = new FocusManager();
  }

}
