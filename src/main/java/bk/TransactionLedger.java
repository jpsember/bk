package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.List;

import bk.gen.Account;
import bk.gen.Alignment;
import bk.gen.Column;
import bk.gen.Datatype;
import bk.gen.ShareCalc;
import bk.gen.Transaction;

public class TransactionLedger extends LedgerWindow implements ChangeListener {

  public TransactionLedger(int accountNumberOrZero, TransactionListener listener) {
    changeManager().addListener(this);
    addColumns();
    mListener = listener;
    // Be careful not to store the actual Account reference, since it may change unexpectedly!
    mAccountNumber = accountNumberOrZero;

    mHeaderType = HEADER_TYPE.GENERAL;
    if (mAccountNumber != 0) {
      mHeaderType = HEADER_TYPE.NORMAL;
      var a = account(mAccountNumber);
      if (a.budget() != 0)
        mHeaderType = HEADER_TYPE.BUDGET;
      else if (a.stock())
        mHeaderType = HEADER_TYPE.STOCK;
    }

    setHeaderHeight(6);
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

  private Account getAccount() {
    if (mAccountNumber == 0)
      return Account.DEFAULT_INSTANCE;
    var a = storage().account(mAccountNumber);
    checkState(a != null, "account number no longer exists:", mAccountNumber);
    return a;
  }

  private enum HEADER_TYPE {
    GENERAL, NORMAL, BUDGET, STOCK
  };

  private HEADER_TYPE mHeaderType;

  @Override
  public void plotHeader(int y, int headerHeight) {
    var a = getAccount();
    var r = Render.SHARED_INSTANCE;
    var clip = r.clipBounds();

    if (mHeaderType == HEADER_TYPE.GENERAL) {
      plotString("All Transactions", clip.x, y, Alignment.CENTER, clip.width);
    } else {
      var s = accountNumberWithNameString(a);
      plotString(s, clip.x, y, Alignment.LEFT, CHARS_ACCOUNT_NUMBER_AND_NAME);
      switch (mHeaderType) {
      case BUDGET: {
        //int budgetSign = budgetSign(a.number());
        long normSpent = budgetSpent(a);
        resetSlotWidth();
        var strBudget = labelledAmount("Budget", a.budget());
        var strSpent = labelledAmount("Spent", normSpent);
        var strAvail = labelledAmount("Avail", a.budget() - normSpent);
        plotInHeaderSlot(strBudget, HEADER_SLOTS - 1, y);
        plotInHeaderSlot(strSpent, 1, y + 1);
        plotInHeaderSlot(strAvail, 2, y + 1);
      }
        break;
      case STOCK: {
        calcShareStuff();
        resetSlotWidth();
        plotShareInfo("All", y, mShareCalcAll);
        plotShareInfo("To cursor", y + 1, mShareCalcToCursor);
        if (mCurrentYear != 0)
          plotInHeaderSlot(
              mCurrentYear+" Gain " + formatDollars(mShareCalcCurrentYear.capGain()), 3, y + 2);
      }
        break;
      case NORMAL: {
        calcBalances();
        resetSlotWidth();
        var strBalance = labelledAmount("Balance", a.balance());
        var strMarked = labelledAmount("Marked", mMarkedBalance);
        var strUnmarked = labelledAmount("Unmarked", a.balance() - mMarkedBalance);
        var strAboveCursor = labelledAmount("Above", mAboveBalance);
        var strBelowCursor = labelledAmount("At,below", mBelowBalance);
        plotInHeaderSlot(strBalance, HEADER_SLOTS - 1, y);
        int y1 = y + 1;
        if (mMarkedCount != 0) {
          plotInHeaderSlot(strMarked, 2, y1 + 1);
          plotInHeaderSlot(strUnmarked, 3, y1 + 1);
        }
        if (currentRowIndex() != 0) {
          plotInHeaderSlot(strAboveCursor, 2, y1);
          plotInHeaderSlot(strBelowCursor, 3, y1);
        }
      }
        break;
      default:
        notSupported();
        break;
      }
    }
    super.plotHeader(y, headerHeight);
  }

  private void plotShareInfo(String prompt, int y, ShareCalc.Builder c) {
    // If there are no transactions, plot nothing
    if (c.numTrans() == 0)
      return;

    plotInHeaderSlot(prompt + ":", 0, y);
    String str;
    if (nonEmpty(c.error())) {
      plotInHeaderSlot(c.error(), 0, y);
    } else {
      str = "Book value " + formatDollars(c.bookValue());
      plotInHeaderSlot(str, 1, y);
      str = "Shares " + String.format("%.03f", c.shares());
      plotInHeaderSlot(str, 2, y);
      str = "Gain " + formatDollars(c.capGain());
      plotInHeaderSlot(str, 3, y);
    }
  }

  private static String formatDollars(double dollars) {
    var curr = dollarsToCurrency(dollars);
    return formatCurrencyEvenZero(curr);
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

  private void calcShareStuff() {
    mShareCalcToCursor = ShareCalc.newBuilder();
    mShareCalcAll = ShareCalc.newBuilder();
    mShareCalcCurrentYear = ShareCalc.newBuilder();
    mCurrentYear = 0;
    double capGainPrevYears = 0;
    {
      Transaction t = getCurrentRow();
      if (t != null) {
        var d = epochSecondsToLocalDate(t.date());
        mCurrentYear = d.getYear();
        pr("currentRow:", getCurrentRow());
      }
    }

    int index = INIT_INDEX;
    for (var t : mDisplayedTransactions) {
      index++;
      log(VERT_SP, "processing transaction:", INDENT, t);
      if (index <= currentRowIndex())
        updateShare(t, mShareCalcToCursor);
      updateShare(t, mShareCalcAll);
      var year = epochSecondsToLocalDate(t.date()).getYear();
      if (year <= mCurrentYear) {
        updateShare(t, mShareCalcCurrentYear);
      }
      if (year < mCurrentYear)
        capGainPrevYears = mShareCalcCurrentYear.capGain();
    }
    mShareCalcCurrentYear.capGain(mShareCalcCurrentYear.capGain() - capGainPrevYears);
  }

  private void updateShare(Transaction t, ShareCalc.Builder c) {
    log(VERT_SP, "updateShare, calc:", INDENT, c);
    c.numTrans(c.numTrans() + 1);
    if (nonEmpty(c.error()))
      return;
    var amt = normalizeTransactionAmount(t);
    var si = parseShareInfo(t.description());

    log("norm amt:", amt);
    log("parsed share info:", INDENT, si);

    switch (si.action()) {
    case NONE:
      return;
    case ERROR:
      c.error("descr: " + t.description());
      break;
    case ASSIGN:
      if (amt != 0) {
        c.error("amount must be zero");
        break;
      }
      if (si.shares() < 0) {
        c.error("assign neg shares");
        break;
      }
      c.shares(si.shares());
      break;
    case BUY:
      if (amt < 0) {
        c.error("amount < 0");
        break;
      }
      if (si.shares() <= 0) {
        c.error("attempt to buy zero or neg shares");
        break;
      }
      checkArgument(si.shares() > 0, "shares <= 0!", INDENT, si);
      c.shares(c.shares() + si.shares());
      c.bookValue(c.bookValue() + currencyToDollars(amt));
      break;
    case SELL: {
      var sellAmt = -amt;
      if (sellAmt <= 0) {
        c.error("spent zero or neg");
        break;
      }
      if (si.shares() < 0) {
        c.error("sell neg shares");
        break;
      }
      if (c.shares() - si.shares() < 0) {
        c.error("shares underflow");
      } else {
        var newBookValue = ((c.shares() - si.shares()) / c.shares()) * c.bookValue();

        log("existing shares:", c.shares());
        log("selling shares :", si.shares());
        log("remaining share:", (c.shares() - si.shares()));
        log("new book value:", newBookValue);

        log("proportion of shares being sold:", si.shares() / c.shares());
        log("book value of that portion:", (si.shares() / c.shares()) * c.bookValue());
        log("sell for:", currencyToDollars(sellAmt));
        var capitalGains = currencyToDollars(sellAmt) - (si.shares() / c.shares()) * c.bookValue();
        log("cap gains:", capitalGains);

        c.shares(c.shares() - si.shares());
        c.bookValue(newBookValue);
        c.capGain(c.capGain() + capitalGains);
      }
    }
      break;
    }
    log("adjusted info:", INDENT, c);
  }

  @Override
  public void plotFooterContent(int y, int height) {
    var r = Render.SHARED_INSTANCE;
    var b = r.clipBounds();
    int x = b.x;
    String msg1;
    String msg2;
    if (mAccountNumber != 0) {
      msg1 = "A:add       ret:edit   opt-z:undo      .:mark/unmark     J:jump";
      msg2 = "opt-d:delete  P:print  opt-Z:redo  opt-.:unmark all    esc:back";
    } else {
      msg1 = "A:add       ret:edit   opt-z:undo      .:mark/unmark";
      msg2 = "opt-d:delete           opt-Z:redo  opt-.:unmark all    esc:back";
    }
    plotString(msg1, x, y);
    plotString(msg2, x, y + 1);
  }

  private static final int HEADER_SLOTS = 4;
  private static final int SLOT_SEP = 4;

  private String labelledAmount(String label, long amount) {
    var s = label + ": " + formatCurrencyEvenZero(amount);
    mSlotWidth = Math.max(SLOT_SEP + s.length(), mSlotWidth);
    return s;
  }

  private void resetSlotWidth() {
    mSlotWidth = SLOT_SEP + 20;
  }

  private void plotInHeaderSlot(String s, int slot, int y) {
    var r = Render.SHARED_INSTANCE;
    var clip = r.clipBounds();
    plotString(s, clip.endX() - (mSlotWidth * (HEADER_SLOTS - slot)), y, Alignment.RIGHT, mSlotWidth);
  }

  private void addColumns() {
    if (mColumnsAdded)
      return;

    boolean alt = mHeaderType == HEADER_TYPE.STOCK;
    var add = alt ? 4 : 0;
    spaceSeparators();
    addColumn(Column.newBuilder().name("Date").datatype(Datatype.DATE).width(CHARS_DATE));
    addColumn(Column.newBuilder().name("Amount").alignment(Alignment.RIGHT).datatype(Datatype.CURRENCY)
        .width(CHARS_CURRENCY));
    addColumn(
        Column.newBuilder().name("Debit").datatype(Datatype.TEXT).width(CHARS_ACCOUNT_NUMBER_AND_NAME - add));
    addColumn(Column.newBuilder().name("Credit").datatype(Datatype.TEXT)
        .width(CHARS_ACCOUNT_NUMBER_AND_NAME - add));
    addColumn(Column.newBuilder().name("Description").datatype(Datatype.TEXT)
        .width(CHARS_TRANSACTION_DESCRIPTION + add));
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

    case KeyEvent.JUMP:
      if (t != null && mAccountNumber != 0) {
        var otherNum = otherAccount(t, mAccountNumber).number();
        mAccountNumber = otherNum;
        rebuild();
      }
      break;

    case KeyEvent.MARK:
      if (t != null) {
        toggleMark(t);
        repaint();
      }
      break;

    case ":M":
      if (mAccountNumber != 0 && !getAllMarkedTransactions().isEmpty()) {
        var form = new AccountRequesterForm("Target account", (f, num) -> {
          pr("ok; moving marked to", num);
          focusManager().pop();
          moveMarked(num, f.addSummaryTransaction());
        });
        form.prepare();
        winMgr().openTreeWithFocus(60, 12, form);
      }
      break;

    case KeyEvent.UNMARK_ALL:
      clearAllMarks();
      repaint();
      break;
    }
    mCurrentTrans = getCurrentRow();
  }

  private void moveMarked(int targetAccountNumber, boolean generateSummaryTransaction) {
    alertVerbose();
    log("moveMarked from:", mAccountNumber, "to:", targetAccountNumber);
    if (targetAccountNumber == mAccountNumber)
      return;

    long summaryBalance = 0;
    long newestMovedDate = 0;

    var u = UndoManager.SHARED_INSTANCE;
    var tids = getAllMarkedTransactions();
    log("marked trans:", tids);
    u.begin("Move Transactions");
    for (var id : tids) {
      var t = storage().transaction(id);
      log("candidate:", INDENT, t);
      if (t.debit() == targetAccountNumber || t.credit() == targetAccountNumber) {
        log("...dr or cr matches target already");
        continue;
      }
      newestMovedDate = Math.max(newestMovedDate, t.date());
      var b = t.toBuilder();
      if (b.debit() == mAccountNumber) {
        b.debit(targetAccountNumber);
        summaryBalance -= t.amount();
      } else if (b.credit() == mAccountNumber) {
        b.credit(targetAccountNumber);
        summaryBalance += t.amount();
      } else
        badState("can't find account number:", mAccountNumber, "in:", INDENT, t);
      log("...adding/replacing:", INDENT, b);
      storage().addOrReplace(b);
      changeManager().registerModifiedTransactions(b);
    }

    if (generateSummaryTransaction && summaryBalance != 0) {
      var tr = Transaction.newBuilder();
      tr.timestamp(storage().uniqueTimestamp());
      tr.date(newestMovedDate);
      if (summaryBalance > 0) {
        tr.amount(summaryBalance);
        tr.credit(mAccountNumber);
        tr.debit(targetAccountNumber);
      } else {
        tr.amount(-summaryBalance);
        tr.debit(mAccountNumber);
        tr.credit(targetAccountNumber);
      }
      tr.description("Moved transactions");
      storage().addOrReplace(tr);
    }
    u.end();
    clearAllMarks();
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

  // For calculating share quantities, cost base, capital gains
  private ShareCalc.Builder mShareCalcToCursor, mShareCalcAll, mShareCalcCurrentYear;
  private int mCurrentYear;
}
