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

  public TransactionLedger(int accountNumberOrZero, TransactionListener listener) {
    changeManager().addListener(this);
    addColumns();
    // mFilter = nullTo(filter, ACCEPT_ALL);
    mListener = listener;
    mAccountNumber = accountNumberOrZero;
    rebuild();
  }
  //
  //  public void prepare(  TransactionListener listener) {
  //    addColumns();
  //    mFilter = nullTo(filter, ACCEPT_ALL);
  //    mListener = listener;
  //    rebuild();
  //  }
  //
  //  public void accountNumber(int anum) {
  //    mAccountNumber = anum;
  //  }

  private void addColumns() {
    if (mColumnsAdded)
      return;
    spaceSeparators();
    addColumn(Column.newBuilder().name("Date").datatype(Datatype.DATE).width(CHARS_DATE));
    addColumn(Column.newBuilder().name("Amount").alignment(Alignment.RIGHT).datatype(Datatype.CURRENCY)
        .width(CHARS_CURRENCY));
    addColumn(Column.newBuilder().name("Debit").datatype(Datatype.TEXT).width(CHARS_ACCOUNT_NUMBER_AND_NAME));
    addColumn(
        Column.newBuilder().name("Credit").datatype(Datatype.TEXT).width(CHARS_ACCOUNT_NUMBER_AND_NAME));
    addColumn(
        Column.newBuilder().name("Description").datatype(Datatype.TEXT).width(CHARS_TRANSACTION_DESCRIPTION));
    mColumnsAdded = true;
  }

  public void rebuild() {
    var currentTrans = getCurrentRow();
    clearEntries();

    List<Transaction> sorted = (mAccountNumber == 0) ? storage().readAllTransactions()
        : storage().readTransactionsForAccount(mAccountNumber);
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

    switch (k.toString()) {

    case KeyEvent.ENTER:
      if (a != null) {
        mListener.editTransaction(mAccountNumber, a);
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
      handled = true;
      break;

    case KeyEvent.EDIT:
      if (a != null) {
        mListener.editTransaction(mAccountNumber, a);
      }
      handled = true;
      break;
    }
    if (!handled)
      super.processKeyEvent(k);
  }

  @Override
  public void dataChanged(List<Integer> accountIds, List<Long> transactionIds) {
    rebuild();
    todo("!verify that it attempts to restore cursor to more or less the same location");
  }

  //  private Filter mFilter;
  private TransactionListener mListener;
  private int mAccountNumber;
  private boolean mColumnsAdded;

}
