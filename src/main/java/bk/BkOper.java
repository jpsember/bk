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

    var screen = screen();
    try {
      loadUtil();
      screen.open();

      var mgr = winMgr();

      var genLedger = buildGeneralLedger();
      var form = buildForm();
      var ourLedger = new LedgerWindow();
      {
        var x = ourLedger;
        x.addColumn(Column.newBuilder().name("Date").datatype(Datatype.DATE));
        x.addColumn(Column.newBuilder().name("Acct").datatype(Datatype.ACCOUNT_NUMBER));
        x.addColumn(Column.newBuilder().name("Name").datatype(Datatype.TEXT).width(25));
        x.addColumn(Column.newBuilder().name("Amount").datatype(Datatype.CURRENCY));
        x.addColumn(Column.newBuilder().name("Description").datatype(Datatype.TEXT).width(40));

        for (var i = 0; i < 20; i++) {
          var t = generateTransaction();
          List<LedgerField> v = arrayList();
          v.add(new DateField(t.date()));
          v.add(new AccountNumberField(t.credit()));
          v.add(new AccountNameField(randomText(15, false)));
          v.add(new CurrencyField(t.amount()));
          v.add(new TextField(t.description()));
          x.addEntry(v);
        }
      }

      // Create a root container
      mgr.pushContainer();
      {
        // Construct two windows; the second has some horizontal panels
        mgr.pct(25);
        mgr.thickBorder();
        mgr.id(WID_GENERAL_LEDGER);
        mgr.handler(genLedger);
        mgr.window();
        mgr.pct(75);
        {
          mgr.horz().pushContainer();
          {
            mgr.chars(15).window();
            mgr.roundedBorder();
            if (false)
              mgr.handler(ourLedger);
            mgr.handler(form);
            mgr.id(WID_LEDGER);
            mgr.pct(80).window();
            mgr.thinBorder();
            mgr.pct(20).window();
          }
          mgr.popContainer();
        }
      }
      mgr.doneConstruction();
      mgr.setFocusWindow(mgr.get(WID_LEDGER));
      screen.mainLoop();
    } catch (Throwable t) {
      setError(screen.closeIfError(t));
    }
  }

  private WindowHandler buildGeneralLedger() {
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

  private FormWindow buildForm() {
    var f = new FormWindow();
    f.addField("Number:", new AccountNumberField(1000));
    f.addField("Name:", new TextField("xxxxxx"));
    f.addOkCancel();
    return f;
  }

  private BkConfig mConfig;

}
