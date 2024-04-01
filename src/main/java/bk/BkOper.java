package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.List;

import bk.gen.BkConfig;
import bk.gen.Column;
import bk.gen.Datatype;
import js.app.AppOper;
import js.base.BasePrinter;

public class BkOper extends AppOper {

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
      String ss[] = { "2024/10/04", "10/04", "10/4", "8/4", "024/10/2", "24/10/2", };
      for (var s : ss) {
        var c = DATE_VALIDATOR.validate(s);
        pr("validated:", INDENT, s, "=>", c);
      }
      halt();
    }

    var mgr = winMgr();

    try {
      mgr.open();

      //  var form = new TransactionForm(); // FormWindow(); //SampleForm();
      //      form.addField("Description");
      //      form.addField("Age");
      //      form.addField("Dr");
      //      form.addField("Cr");

      LedgerWindow genLedger = buildGeneralLedger();

      // Create a root container
      mgr.pushContainer();
      {

        {
          // Construct ledger
          mgr.pct(100);
          mgr.thickBorder();
          mgr.window(genLedger);
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

  private LedgerWindow buildGeneralLedger() {
    var lg = new LedgerWindow();
    {
      final int NAMED_ACCOUNT_WIDTH = 25;
      var x = lg;
      x.addColumn(Column.newBuilder().name("Date").datatype(Datatype.DATE));
      x.addColumn(VERT_SEP);
      x.addColumn(Column.newBuilder().name("Amount").datatype(Datatype.CURRENCY));
      x.addColumn(VERT_SEP);
      x.addColumn(Column.newBuilder().name("Dr").datatype(Datatype.TEXT).width(NAMED_ACCOUNT_WIDTH));
      x.addColumn(VERT_SEP);
      x.addColumn(Column.newBuilder().name("Cr").datatype(Datatype.TEXT).width(NAMED_ACCOUNT_WIDTH));
      x.addColumn(VERT_SEP);
      x.addColumn(Column.newBuilder().name("Description").datatype(Datatype.TEXT).width(40));

      for (var i = 0; i < 20; i++) {
        var t = generateTransaction();
        List<LedgerField> v = arrayList();
        v.add(new DateField(t.date()));
        v.add(VERT_SEP_FLD);
        v.add(new CurrencyField(t.amount()));
        v.add(VERT_SEP_FLD);
        v.add(new AccountNameField(t.debit(), randomText(20, false)));
        v.add(VERT_SEP_FLD);
        v.add(new AccountNameField(t.credit(), randomText(20, false)));
        v.add(VERT_SEP_FLD);
        v.add(new TextField(t.description()));
        x.addEntry(v);
      }
    }
    return lg;

  }

  private BkConfig mConfig;

}
