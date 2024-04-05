package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.List;

import bk.gen.Alignment;
import bk.gen.Column;
import bk.gen.Datatype;
import bk.gen.Transaction;

public class TransactionLedger extends LedgerWindow implements ChangeListener {

  public interface Filter {
    boolean accept(Transaction t);
  }

  public static final Filter ACCEPT_ALL = (t) -> true;

  public TransactionLedger() {
    changeManager().addListener(this);
  }

  public void prepare(Filter filter, TransactionListener listener) {
    addColumns();
    mFilter = nullTo(filter, ACCEPT_ALL);
    mListener = listener;
    rebuild();
  }

  public void accountNumber(int anum) {
    mAccountNumber = anum;
  }

  private void addColumns() {
    if (mColumnsAdded)
      return;
    addColumn(Column.newBuilder().name("Date").datatype(Datatype.DATE).width(CHARS_DATE));
    addColumn(Column.newBuilder().name("Amount").alignment(Alignment.RIGHT).datatype(Datatype.CURRENCY)
        .width(CHARS_CURRENCY));
    addColumn(Column.newBuilder().name("Dr").datatype(Datatype.TEXT).width(CHARS_ACCOUNT_NUMBER_AND_NAME));
    addColumn(Column.newBuilder().name("Cr").datatype(Datatype.TEXT).width(CHARS_ACCOUNT_NUMBER_AND_NAME));
    addColumn(
        Column.newBuilder().name("Description").datatype(Datatype.TEXT).width(CHARS_TRANSACTION_DESCRIPTION));
    mColumnsAdded = true;
  }

  public void rebuild() {
    checkState(prepared());
    var currentTrans = getCurrentRow();
    clearEntries();

    var trans = storage().transactions();
    List<Transaction> sorted = arrayList();
    for (var t : trans.values())
      if (mFilter.accept(t)) {
        sorted.add(t);
        //    pr("adding transaction to be sorted:",INDENT,t);
      }
    sorted.sort(TRANSACTION_COMPARATOR);

    for (var t : sorted) {
      openEntry();
      add(new DateField(t.date()));
      add(new CurrencyField(t.amount()));
      add(new AccountNameField(t.debit(), storage().accountName(t.debit())));
      add(new AccountNameField(t.credit(), storage().accountName(t.credit())));
      add(new TextField(t.description()));
      closeEntry(t);
    }
    setCurrentRow(currentTrans);
    repaint();
  }

  @Override
  public void processKeyEvent(KeyEvent k) {
    boolean handled = false;
    Transaction a = getCurrentRow();

    todo("the 'rebuild' should be a more general call for all, either add, edit, or delete");
    switch (k.toString()) {

    case KeyEvent.ENTER:
      if (a != null) {
        mListener.editTransaction(mAccountNumber, a);
        rebuild();
        handled = true;
      }
      break;

    case KeyEvent.DELETE_TRANSACTION:
      if (a != null) {
        mListener.deleteTransaction(a);
      }
      handled = true;
      break;

    case KeyEvent.ADD:
      mListener.addTransaction(mAccountNumber);
      rebuild();
      handled = true;
      break;

    case KeyEvent.EDIT:
      if (a != null) {
        mListener.editTransaction(mAccountNumber, a);
        rebuild();
      }
      handled = true;
      break;
    }
    if (!handled)
      super.processKeyEvent(k);
  }

  @Override
  public void dataChanged(List<Integer> accountIds, List<Long> transactionIds) {
    if (!prepared())
      return;
    rebuild();
    todo("!verify that it attempts to restore cursor to more or less the same location");
  }

  private boolean prepared() {
    return mFilter != null;
  }

  private Filter mFilter;
  private TransactionListener mListener;
  private int mAccountNumber;
  private boolean mColumnsAdded;

}
