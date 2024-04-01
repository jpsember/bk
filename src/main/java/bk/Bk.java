package bk;

import static js.base.Tools.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;

import js.app.App;
import js.base.DateTimeTools;
import js.base.LoggerInterface;
import static bk.Util.*;

public class Bk extends App {

  
  public static void main(String[] args) {
    var logger = new OurTextLogger();
   if (!EXP)
     logger(logger);
    try {
      loadTools();
      Bk app = new Bk();
      //app.setCustomArgs("-v");
      app.startApplication(args);
      app.exitWithReturnCode();
    } finally {
      logger.println("...Bk is exiting");
    }
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  protected void registerOperations() {
    registerOper(new BkOper());
  }

  private static class OurTextLogger implements LoggerInterface {

    public OurTextLogger() {
      try {
        var fw = new FileWriter("_SKIP_log.txt");
        mWriter = new BufferedWriter(fw);
        mWriter.append("\n\nBk Writer opened at: " + DateTimeTools.humanTimeString()
            + "\n--------------------------------------------------\n");
      } catch (Throwable t) {
        throw asRuntimeException(t);
      }
    }

    private Writer mWriter;

    @Override
    public void println(String message) {
      try {
        mWriter.append(message);
        mWriter.write('\n');
        mWriter.flush();
      } catch (Throwable t) {
        throw asRuntimeException(t);
      }
    }
  }
}
