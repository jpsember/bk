package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.List;

import com.googlecode.lanterna.input.KeyStroke;

import bk.gen.Column;
import bk.gen.Datatype;
import bk.gen.Transaction;

public class TransactionLedger extends LedgerWindow implements ChangeListener {

  public interface Filter {
    boolean accept(Transaction t);
  }

  public static final Filter ACCEPT_ALL = (t) -> true;

  public TransactionLedger(Filter filter, TransactionListener listener) {
    mFilter = nullTo(filter, ACCEPT_ALL);
    mListener = listener;
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

  public void rebuild() {

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
    Transaction a = getCurrentRow();

    todo("the 'rebuild' should be a more general call for all, either add, edit, or delete");
    switch (k.getKeyType()) {

    case Enter:
      if (a != null) {
        mListener.editTransaction(mAccountNumber, a);
        rebuild();
        handled = true;
      }
      break;

    case Character: {
      switch (getCharSummary(k)) {

      case KEY_DELETE_TRANSACTION:
        if (a != null) {
          mListener.deleteTransaction(a);
        }
        handled = true;
        break;

      case ":a":
        mListener.addTransaction(mAccountNumber);
        rebuild();
        handled = true;
        break;
      case ":e":
        if (a != null) {
          mListener.editTransaction(mAccountNumber, a);
          rebuild();
        }
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

  private Filter mFilter;
  private TransactionListener mListener;
  private int mAccountNumber;

  @Override
  public void dataChanged(List<Integer> accountIds, List<Long> transactionIds) {
rebuild();
todo("!verify that it attempts to restore cursor to more or less the same location");
  }
}
