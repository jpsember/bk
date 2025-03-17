package bk;

import static js.base.Tools.*;

import js.app.App;
import js.app.HelpFormatter;
import js.base.BasePrinter;
import js.data.DataUtil;

public class Bk extends App {

  public static void main(String[] args) {
    loadTools();
    Bk app = new Bk();
    
    //  app. setCustomArgs("close_accounts 2024/6/15");
    
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
