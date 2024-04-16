package bk;

import static js.base.Tools.*;

import js.app.App;

public class Bk extends App {

  public static void main(String[] args) {
    loadTools();
    var x = com.lowagie.text.Element.ALIGN_BASELINE;
    pr(x);
    Bk app = new Bk();
    app.startApplication(args);
    app.exitWithReturnCode();
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  protected void registerOperations() {
    registerOper(new BkOper());
  }

}
