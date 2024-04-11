package bk;

import static js.base.Tools.*;

import bk.gen.Account;

import static bk.Util.*;

public class PrintManager {
  public static final PrintManager SHARED_INSTANCE = new PrintManager();

  public void printLedger(Account a) {
    loadTools();
    loadUtil();
    mark("finish this");
  }
}
