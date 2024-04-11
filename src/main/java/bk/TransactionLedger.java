package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.List;

import bk.gen.Account;
import bk.gen.Alignment;
import bk.gen.Column;
import bk.gen.Datatype;
import bk.gen.Transaction;

public class TransactionLedger extends LedgerWindow implements ChangeListener {

  public TransactionLedger(int accountNumberOrZero, TransactionListener listener) {
    changeManager().addListener(this);
    addColumns();
    mListener = listener;
    // Be careful not to store the actual Account reference, since it may change unexpectedly!
    mAccountNumber = accountNumberOrZero;
    setHeaderHeight(hasBudget() ? 5 : 4);
    rebuild();
  }

  @Override
  public boolean focusPossible() {
    return mAccountNumber == 0 || account(mAccountNumber) != null;
  }

  @Override
  public boolean undoEnabled() {
    return true;
  }

  private boolean hasBudget() {
    return getAccount().budget() != 0;
  }

  private Account getAccount() {
    if (mAccountNumber == 0)
      return Account.DEFAULT_INSTANCE;
    var a = storage().account(mAccountNumber);
    checkState(a != null, "account number no longer exists:", mAccountNumber);
    return a;
  }

  @Override
  public void plotHeader(int y, int headerHeight) {
    var mAccount = getAccount();
    var r = Render.SHARED_INSTANCE;
    var clip = r.clipBounds();
    var a = mAccount;
    if (a == null) {
      plotString("All Transactions", clip.x, y, Alignment.CENTER, clip.width);
    } else {
      var s = a.number() + " " + a.name();
      plotString(s, clip.x, y, Alignment.LEFT, CHARS_ACCOUNT_NUMBER_AND_NAME);
      if (hasBudget()) {
        plotLabelledAmount("Budget", a.budget(), 1, y);
        plotLabelledAmount("Spent", a.balance(), 0, y + 1);
        plotLabelledAmount("Avail", a.budget() - a.balance(), 1, y + 1);
      } else {
        plotLabelledAmount("Balance", a.balance(), 1, y);
      }
    }
    super.plotHeader(y, headerHeight);
  }

  private void plotLabelledAmount(String label, long amount, int slot, int y) {
    var s = leftPad(label + ": ", 9) + leftPad(formatCurrencyEvenZero(amount), CHARS_CURRENCY);
    var r = Render.SHARED_INSTANCE;
    var clip = r.clipBounds();
    var CHARS_SLOT = 34;
    plotString(s, clip.endX() - (CHARS_SLOT * (2 - slot)), y, Alignment.RIGHT, CHARS_SLOT);
  }

  private String leftPad(String str, int minLength) {
    return spaces(minLength - str.length()) + str;
  }

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

    case KeyEvent.EDIT:
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

    }
    if (!handled)
      super.processKeyEvent(k);
  }

  @Override
  public void dataChanged(List<Integer> accountIds, List<Long> transactionIds) {
    // mark("data changed in:", this, "; accountIds:", accountIds, "trans:", transactionIds, ST);
    rebuild();
    todo("!verify that it attempts to restore cursor to more or less the same location");
  }

  private TransactionListener mListener;
  private int mAccountNumber;
  private boolean mColumnsAdded;
}
