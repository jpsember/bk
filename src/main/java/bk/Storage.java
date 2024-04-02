package bk;

import static js.base.Tools.*;

import java.io.File;
import java.util.Map;

import bk.gen.Database;
import bk.gen.Transaction;
import js.base.BaseObject;
import js.file.Files;
import js.json.JSMap;

public class Storage extends BaseObject {

  private static final String KEY_ACCOUNTS = "accounts";

  public void read() {
    alertVerbose();
    var m = JSMap.fromFileIfExists(file());
    var db = Files.parseAbstractData(Database.DEFAULT_INSTANCE, m);
    mDatabase = db.toBuilder();
    mAccountBalanceMap = hashMap();
    log("read", accounts().size(), "accounts and", transactions().size(), "transactions");
  }

  public void setModified() {
    mModified = true;
  }

  public void flush() {
    if (!mModified)
      return;
    Files.S.writeWithPrettyIf(file(), mDatabase, alert("writing pretty"));
    mModified = false;
  }

  private File file() {
    if (mFile == null) {
      mFile = new File("accounts.json");
      log("storage file:", INDENT, Files.infoMap(mFile));
    }
    return mFile;
  }

  public Map<Integer, String> accounts() {
    return mDatabase.accounts();
  }

  public Map<Long, Transaction> transactions() {
    return mDatabase.transactions();
  }

  public int accountBalance(int accountNumber) {
    Integer key = accountNumber;
    var value = mAccountBalanceMap.get(key);
    if (value == null) {
      long sum = 0;
      for (var trans : transactions().values()) {
        if (trans.debit() == accountNumber) {
          sum += trans.amount();
        } else if (trans.credit() == accountNumber) {
          sum -= trans.amount();
        }
      }
      value = (int) sum;
      if (value != sum)
        badState("account balance has overflowed:", accountNumber);
      mAccountBalanceMap.put(key, value);
    }
    return value;
  }

  public String accountName(int accountNumber) {
    var acct = mDatabase.accounts().get(accountNumber);
    if (acct == null) {
      return "<not found!>";
    }
    return acct;
  }

  private Database.Builder mDatabase;
  private Map<Integer, Integer> mAccountBalanceMap;
  private File mFile;
  private boolean mModified;
}
