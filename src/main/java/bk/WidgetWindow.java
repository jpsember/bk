package bk;

public abstract class WidgetWindow extends JWindow implements FocusHandler {

  public void width(int width) {
    mWidth = width;
  }

  public int width() {
    return mWidth;
  }

  private int mWidth = 16;
}
