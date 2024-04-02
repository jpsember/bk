package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.List;

import com.googlecode.lanterna.input.KeyStroke;

import bk.gen.Column;
import bk.gen.Datatype;
import bk.gen.Transaction;

public class GeneralLedger extends LedgerWindow {

  public GeneralLedger() {
    build();
  }

  private void build() {

    final int NAMED_ACCOUNT_WIDTH = 25;
    addColumn(Column.newBuilder().name("Date").datatype(Datatype.DATE));
    addColumn(VERT_SEP);
    addColumn(Column.newBuilder().name("Amount").datatype(Datatype.CURRENCY));
    addColumn(VERT_SEP);
    addColumn(Column.newBuilder().name("Dr").datatype(Datatype.TEXT).width(NAMED_ACCOUNT_WIDTH));
    addColumn(VERT_SEP);
    addColumn(Column.newBuilder().name("Cr").datatype(Datatype.TEXT).width(NAMED_ACCOUNT_WIDTH));
    addColumn(VERT_SEP);
    addColumn(Column.newBuilder().name("Description").datatype(Datatype.TEXT).width(40));

    var trans = storage().transactions();
    List<Transaction> sorted = arrayList();
    sorted.addAll(trans.values());
    sorted.sort(TRANSACTION_COMPARATOR);

    for (var t : sorted) {
      List<LedgerField> v = arrayList();
      v.add(new DateField(t.date()));
      v.add(VERT_SEP_FLD);
      v.add(new CurrencyField(t.amount()));
      v.add(VERT_SEP_FLD);
      v.add(new AccountNameField(t.debit(), storage().accountName(t.debit())));
      v.add(VERT_SEP_FLD);
      v.add(new AccountNameField(t.credit(), storage().accountName(t.credit())));
      v.add(VERT_SEP_FLD);
      v.add(new TextField(t.description()));
      addEntry(v);
    }
  }

  @Override
  public void processKeyStroke(KeyStroke k) {
    boolean handled = false;
    switch (k.getKeyType()) {
    case Character: {
      var ch = k.getCharacter();
      switch (ch) {
      case 'a':
        TransactionForm.addTransaction();
        handled = true;
        break;
      }
    }
      break;
    default:
      break;
    }
    if (!handled)
      super.processKeyStroke(k);
  }
}
