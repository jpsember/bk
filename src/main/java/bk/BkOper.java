package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

import bk.gen.Account;
import bk.gen.BkConfig;
import js.app.AppOper;
import js.base.BasePrinter;

public class BkOper extends AppOper implements AccountListListener, AccountForm.Listener {

  @Override
  public String userCommand() {
    return "bk";
  }

  @Override
  protected String shortHelp() {
    return "Bookkeeping program";
  }

  @Override
  public BkConfig defaultArgs() {
    return BkConfig.DEFAULT_INSTANCE;
  }

  @Override
  protected void longHelp(BasePrinter b) {
    todo("more longHelp to come later...");
    super.longHelp(b);
  }

  @Override
  public BkConfig config() {
    if (mConfig == null)
      mConfig = (BkConfig) super.config();
    return mConfig;
  }

  @Override
  public void perform() {

    if (EXP) {

      // We only want to use the local time zone to determine the default date as a string, not including any time stuff

      // We want to store YYYY/MM/DD that is independent of time zone where it is viewed.  So we will work with zulu time.

      var ld = LocalDate.now();
      pr("local date:", ld);
      var year = ld.getYear();
      var month = ld.getMonthValue();
      var day = ld.getDayOfMonth();

      //      var dt = ld.atStartOfDay(ZoneId.systemDefault());
      //      pr("LocalDate.now:", ld);
      //      pr("LocalDateTime at start of day:", dt);
      pr("local year:", year, "m:", month, "day:", day);

      ZoneId zoneId = ZoneId.systemDefault();

      DateTimeFormatter dateParser = new DateTimeFormatterBuilder().appendPattern("u/M/d")
          .parseDefaulting(ChronoField.YEAR_OF_ERA, year) //sCurrentDate.getYear())
          .parseDefaulting(ChronoField.HOUR_OF_DAY, 0).parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
          .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0).toFormatter();
      LocalDate parsed = dateParser.parse(year + "/" + month + "/" + day, LocalDate::from);

      pr("parsed:", parsed, parsed.getClass());
      var epochMilli = parsed.atStartOfDay(zoneId).toInstant().toEpochMilli();
      pr("epoch milli:", epochMilli);
      checkState(epochMilli % 1000 == 0);

      int epochSeconds = (int) (epochMilli / 1000);
      pr("epoch seconds:", epochSeconds);

      DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().appendPattern("uuuu/MM/dd")
          .toFormatter();

      // Convert epoch seconds back to local date time
      LocalDate date = Instant.ofEpochSecond(epochSeconds).atZone(zoneId).toLocalDate();
      var str2 = dateFormatter.format(date);
      pr("converted back to string:", str2);
      //
      //pr("parsed:",parsed,parsed.getClass());
      //
      // parsed.
      // .toInstant().toEpochMilli();
      //

      //
      //
      //
      //
      //
      //
      //       
      ////      var es = dt.toEpochSecond();
      ////      pr("epoch second:", es);
      //
      ////      long epsec = 1712041200L;
      //
      //      DateTimeFormatter fmt = new DateTimeFormatterBuilder().appendPattern("u/M/d")
      //          .parseDefaulting(ChronoField.YEAR_OF_ERA, year) //sCurrentDate.getYear())
      //          .parseDefaulting(ChronoField.HOUR_OF_DAY, 0).parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
      //          .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0).toFormatter().withZone(ZoneId.of("UTC"));
      //
      //      // fmt .withZone(ZoneId.systemDefault());
      //      
      //      month = 10;
      //      day = 31;
      //      var defs =year+"/"+ month+"/"+day;
      //     var defStr = fmt.parse(defs);
      //     pr("parsed:",defs,"as:",defStr);
      //     
      //     
      //     var yr2 = defStr.get(ChronoField.YEAR_OF_ERA);
      //     var mth2 = defStr.get(ChronoField.MONTH_OF_YEAR);
      //     var day2 = defStr.get(ChronoField.DAY_OF_MONTH);
      //pr("extracted:",yr2,"/",mth2,"/",day2);     
      //     
      ////      var q = fmt.format(Instant.ofEpochSecond(epsec));
      ////      pr("formatted:", q);
      //      halt();
      //      String ss[] = { "", "2024/10/04", "10/04", "10/4", "8/4", "024/10/2", "24/10/2", };
      //      for (var s : ss) {
      //        pr(VERT_SP, "about to validate:", s);
      //        var c = DATE_VALIDATOR.validate(s);
      //        pr("validated:", INDENT, s, "=>", c);
      //      }
      halt();
    }

    storage().read();

    var mgr = winMgr();

    try {
      mgr.open();
      mAccounts = new AccountList(this);
      mTransactions = new TransactionLedger(null);
      sAccountsView = mAccounts;
      sTransactionsView = mTransactions;

      // Create a root container
      mgr.pushContainer();
      {

        {
          // Construct ledger
          mgr.pct(100);
          mgr.thinBorder();
          mgr.window(mAccounts);
        }
        //        mgr.pct(75);
        //        {
        //          //mgr.horz().pushContainer();
        //          {
        //            // mgr.chars(15).window();
        //            mgr.roundedBorder();
        //            //            if (false)
        //            //              mgr.handler(ourLedger);
        //            // mgr.handler(form);
        //            mgr.window(form);
        //            //            mgr.thinBorder();
        //            //            mgr.pct(20).window();
        //          }
        //          //mgr.popContainer();
        //        }
      }
      mgr.doneConstruction();
      mgr.mainLoop();
    } catch (Throwable t) {
      setError(mgr.closeIfError(t));
    }
  }

  private BkConfig mConfig;
  private AccountList mAccounts;
  private TransactionLedger mTransactions;

  // ------------------------------------------------------------------
  // AccountListListener
  // ------------------------------------------------------------------

  @Override
  public void editAccount(Account account) {
    var form = new AccountForm(AccountForm.TYPE_EDIT, account, this);
    addToMainView(form);
  }

  @Override
  public void addAccount() {
    var form = new AccountForm(AccountForm.TYPE_ADD, null, this);
    addToMainView(form);
  }

  @Override
  public void viewAccount(Account account) {
    todo("view account:", account);
    var ledger = new TransactionLedger(
        (t) -> t.credit() == account.number() || t.debit() == account.number());
    switchToView(ledger);
  }

  //------------------------------------------------------------------
  // AccountFormListener
  // ------------------------------------------------------------------

  @Override
  public void editedAccount(AccountForm form, Account account) {

    form.remove();

    if (account == null)
      return;
    pr("editedAccount:", INDENT, account);
    mAccounts.rebuild();
    mAccounts.setCurrentRow(account);
    mAccounts.repaint();

    // Restore focus to the AccountList
    focusManager().set(mAccounts);
  }

}
