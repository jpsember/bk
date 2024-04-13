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

    setFooterHeight(3);
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
    var a = getAccount();
    var r = Render.SHARED_INSTANCE;
    var clip = r.clipBounds();
    if (a.number() == 0) {
      plotString("All Transactions", clip.x, y, Alignment.CENTER, clip.width);
    } else {
      var s = accountNumberWithNameString(a);
      plotString(s, clip.x, y, Alignment.LEFT, CHARS_ACCOUNT_NUMBER_AND_NAME);
      if (hasBudget()) {
        long spent = -a.balance();
        plotLabelledAmount("Budget", a.budget(), 1, y);
        plotLabelledAmount("Spent", spent, 0, y + 1);
        plotLabelledAmount("Avail", a.budget() - spent, 1, y + 1);
      } else {
        plotLabelledAmount("Balance", a.balance(), 1, y);
      }
    }
    super.plotHeader(y, headerHeight);
  }

  @Override
  public void plotFooterContent(int y, int height) {
    var r = Render.SHARED_INSTANCE;
    var b = r.clipBounds();
    int x = b.x;
    plotString("A:add       ret:edit   opt-z:undo", x, y);
    var msg = "opt-d:delete  P:print  opt-Z:redo  esc:back";
    if (mAccountNumber == 0)
      msg.replace("P:print", "       ");
    plotString(msg, x, y + 1);
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

      var amt = t.amount();
      // Adjust sign based on which of debit or credit matches the current account 
      if (mAccountNumber != 0) {
        if (t.credit() == mAccountNumber)
          amt = -amt;
      } else {
        todo("what do we do here for the general ledger?");
      }

      add(new CurrencyField(amt));
      add(new AccountNameField(t.debit(), storage().accountName(t.debit())));
      add(new AccountNameField(t.credit(), storage().accountName(t.credit())));
      add(new TextField(t.description()));
      closeEntry(t);
    }
    setCurrentRow(currentTrans);
    repaint();
  }

  @Override
  public int chooseCurrentRow() {
    int bestMatch = 0;
    if (mCurrentTrans != null) {
      int x = size();
      for (int i = 0; i < x; i++) {
        Transaction t = entry(i);
        if (t.date() >= mCurrentTrans.date()) {
          bestMatch = i;
          break;
        }
      }
    }
    return bestMatch;
  }

  @Override
  public void processKeyEvent(KeyEvent k) {
    boolean handled = false;
    Transaction t = getCurrentRow();

    switch (k.toString()) {

    case KeyEvent.EDIT:
    case KeyEvent.RETURN:
      if (t != null && !isGenerated(t)) {
        mListener.editTransaction(mAccountNumber, t);
      }
      handled = true;
      break;

    case KeyEvent.DELETE_TRANSACTION:
      if (t != null) {
        if (!isGenerated(t))
          mListener.deleteTransaction(t);
      }
      handled = true;
      break;

    case KeyEvent.ADD:
    case ":a":
      mListener.addTransaction(mAccountNumber);
      handled = true;
      break;

    case KeyEvent.PRINT:
      handled = true;
      if (mAccountNumber != 0) {
        PrintManager.SHARED_INSTANCE.printLedger(getAccount());
      }
      break;
    }
    if (!handled)
      super.processKeyEvent(k);
    mCurrentTrans = getCurrentRow();
  }

  @Override
  public void dataChanged(List<Integer> accountIds, List<Long> transactionIds) {
    rebuild();
  }

  private TransactionListener mListener;
  private int mAccountNumber;
  private boolean mColumnsAdded;
  private Transaction mCurrentTrans;
}
