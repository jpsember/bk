package bk;

public class WidgetWindow extends JWindow implements FocusHandler {

  public WidgetWindow width(int width) {
    mWidth = width;
    return this;
  }

  public int width() {
    return mWidth;
  }

  public WidgetWindow label(String label) {
    mLabel = label;
    return this;
  }

  @Override
  public void paint() {
    var r = Render.SHARED_INSTANCE;
    var b = r.clipBounds();
    b = b.withInset(1, 0);
    var SEP = 1;
    var labelWidth = b.width / 2;
    var valueWidth = b.width - SEP - labelWidth;
    {
      var ef = mLabel + ":";
      r.drawString(b.x + labelWidth - ef.length(), b.y, labelWidth, ef);
    }
    {
      var val = "value_here";
      r.drawString(b.x + labelWidth + SEP, b.y, valueWidth, val);
    }
  }

  private int mWidth = 16;
  private String mLabel = "<no label!>";

}
