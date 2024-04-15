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
    setHeaderHeight(5);
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

  @Override
  public boolean isItemMarked(Object auxData) {
    return isMarked((Transaction) auxData);
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
        resetSlotWidth();
        var strBudget = labelledAmount("Budget", a.budget());
        var strSpent = labelledAmount("Spent", spent);
        var strAvail = labelledAmount("Avail", a.budget() - spent);

        plotLabelledAmount(strBudget, HEADER_SLOTS - 1, y);
        plotLabelledAmount(strSpent, 0, y + 1);
        plotLabelledAmount(strAvail, 1, y + 1);
      } else {
        calcBalances();
        resetSlotWidth();
        var strBalance = labelledAmount("Balance", a.balance());
        var strMarked = labelledAmount("Marked", mMarkedBalance);
        var strAboveCursor = labelledAmount("Above", mAboveBalance);
        var strBelowCursor = labelledAmount("At,below", mBelowBalance);
        plotLabelledAmount(strBalance, HEADER_SLOTS - 1, y);
        int y1 = y + 1;
        if (mMarkedCount != 0)
          plotLabelledAmount(strMarked, 0, y1);
        if (currentRowIndex() != 0) {
          plotLabelledAmount(strAboveCursor, 1, y1);
          plotLabelledAmount(strBelowCursor, 2, y1);
        }
      }
    }
    super.plotHeader(y, headerHeight);
  }

  private void calcBalances() {
    long balMarked = 0;
    long balAbove = 0;
    mMarkedCount = 0;

    int index = INIT_INDEX;
    for (var t : mDisplayedTransactions) {
      var amt = normalizeTransactionAmount(t);
      index++;
      if (isMarked(t)) {
        mMarkedCount++;
        balMarked += amt;
      }
      if (index < currentRowIndex())
        balAbove += amt;
    }
    mAboveBalance = balAbove;
    mBelowBalance = getAccount().balance() - mAboveBalance;
    mMarkedBalance = balMarked;
  }

  @Override
  public void plotFooterContent(int y, int height) {
    var r = Render.SHARED_INSTANCE;
    var b = r.clipBounds();
    int x = b.x;
    plotString(//
        "A:add       ret:edit   opt-z:undo      .:mark/unmark", x, y);
    var msg = //
        "opt-d:delete  P:print  opt-Z:redo  opt-.:unmark all    esc:back";
    if (mAccountNumber == 0)
      msg.replace("P:print", "       ");
    plotString(msg, x, y + 1);
  }

  private static final int HEADER_SLOTS = 3;
  private static final int SLOT_SEP = 4;

  private String labelledAmount(String label, long amount) {
    var s = label + ": " + formatCurrencyEvenZero(amount);
    mSlotWidth = Math.max(SLOT_SEP + s.length(), mSlotWidth);
    return s;
  }

  private void resetSlotWidth() {
    mSlotWidth = SLOT_SEP + 20;
  }

  private void plotLabelledAmount(String s, int slot, int y) {
    var r = Render.SHARED_INSTANCE;
    var clip = r.clipBounds();
    plotString(s, clip.endX() - (mSlotWidth * (HEADER_SLOTS - slot)), y, Alignment.RIGHT, mSlotWidth);
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
    mDisplayedTransactions = sorted;
    for (var t : sorted) {
      openEntry();
      add(new DateField(t.date()));

      var amt = normalizeTransactionAmount(t);

      add(new CurrencyField(amt));
      add(new AccountNameField(t.debit(), storage().accountName(t.debit())));
      add(new AccountNameField(t.credit(), storage().accountName(t.credit())));
      add(new TextField(t.description()));
      closeEntry(t);
    }
    setCurrentRow(currentTrans);

    repaint();
  }

  private long normalizeTransactionAmount(Transaction t) {
    long amt = t.amount();
    // If ledger is for a particular account, negate sign
    // based on which of debit or credit matches the current account 
    if (mAccountNumber != 0) {
      if (t.credit() == mAccountNumber)
        amt = -amt;
    }
    return amt;
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
    Transaction t = getCurrentRow();

    switch (k.toString()) {

    default:
      super.processKeyEvent(k);
      break;

    case KeyEvent.EDIT:
    case KeyEvent.RETURN:
      if (t != null && !isGenerated(t)) {
        mListener.editTransaction(mAccountNumber, t);
      }
      break;

    case KeyEvent.DELETE_TRANSACTION:
      if (t != null) {
        if (!isGenerated(t))
          mListener.deleteTransaction(t);
      }
      break;

    case KeyEvent.ADD:
    case ":a":
      mListener.addTransaction(mAccountNumber);
      break;

    case KeyEvent.PRINT:
      if (mAccountNumber != 0) {
        PrintManager.SHARED_INSTANCE.printLedger(getAccount());
      }
      break;

    case KeyEvent.MARK:
      if (t != null) {
        toggleMark(t);
        repaint();
      }
      break;

    }
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
  private long mMarkedBalance;
  private int mMarkedCount;
  private long mAboveBalance;
  private long mBelowBalance;
  private List<Transaction> mDisplayedTransactions = arrayList();
  private int mSlotWidth;
}
