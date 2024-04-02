package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.List;

import com.googlecode.lanterna.input.KeyStroke;

import bk.gen.Column;
import bk.gen.Datatype;
import bk.gen.Transaction;

public class TransactionLedger extends LedgerWindow {

  public interface Filter {
    boolean accept(Transaction t);
  }

  public static final Filter ACCEPT_ALL = (t) -> true;

  public TransactionLedger(Filter filter) {
    mFilter = nullTo(filter, ACCEPT_ALL);
    addColumns();
    rebuild();
  }

  public void accountNumber(int anum) {
    mAccountNumber = anum;
  }

  private void addColumns() {
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
  }

  private void rebuild() {

    var currentTrans = getCurrentRow();
    clearEntries();

    var trans = storage().transactions();
    List<Transaction> sorted = arrayList();
    for (var t : trans.values())
      if (mFilter.accept(t))
        sorted.add(t);
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
      addEntry(v, t);
    }
    setCurrentRow(currentTrans);
    repaint();
  }

  @Override
  public void processKeyStroke(KeyStroke k) {
    boolean handled = false;
    switch (k.getKeyType()) {
    case Character: {
      switch (getCharSummary(k)) {
      case ":a": {
        var f = TransactionForm.buildAddTransaction();
        f.forAccount(mAccountNumber);
        addToMainView(f);
        handled = true;
        
      }
        break;
      case ":e": {
        Transaction a = getCurrentRow();
        if (a != null) {
          var f = TransactionForm.buildEditTransaction(a);
          f.forAccount(mAccountNumber);
        }
        handled = true;
      }
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

  private Filter mFilter;
  private int mAccountNumber;
}
