package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.List;

import bk.gen.ShareCalc;
import bk.gen.Transaction;
import js.base.BaseObject;

public class StockCalculator extends BaseObject {

  public StockCalculator withAccountNumber(int accountNumber) {
    assertNotCalc();
    mAccountNumber = accountNumber;
    return this;
  }

  public StockCalculator withTransactions(List<Transaction> transactions) {
    assertNotCalc();
    mTransactions = transactions;
    return this;
  }

  public StockCalculator withCursor(int cursorRow) {
    assertNotCalc();
    mCursorRow = cursorRow;
    return this;
  }

  private void assertNotCalc() {
    checkState(mShareCalcAll == null, "already calculated");
  }

 private void calculate() {
    if (mShareCalcAll != null)
      return;

    mShareCalcToCursor = ShareCalc.newBuilder();
    mShareCalcAll = ShareCalc.newBuilder();
    mShareCalcCurrentYear = ShareCalc.newBuilder();

    double capGainPrevYears = 0;

    if (mCursorRow >= 0) {
      Transaction t = mTransactions.get(mCursorRow);
      var d = epochSecondsToLocalDate(t.date());
      mCurrentYear = d.getYear();
    }

    int index = INIT_INDEX;
    for (var t : mTransactions) {
      index++;
      log(VERT_SP, "processing transaction:", INDENT, t);
      if (index <= mCursorRow)
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

  public ShareCalc toCursor() {
    calculate();
    checkState(mCursorRow >= 0);
    return mShareCalcToCursor;
  }

  public ShareCalc all() {
    calculate();
    return mShareCalcAll;
  }

  public ShareCalc forCurrentYear() {
    calculate();
    checkState(mCursorRow >= 0);
    return mShareCalcCurrentYear;
  }

  // For calculating share quantities, cost base, capital gains
  private ShareCalc.Builder mShareCalcToCursor, mShareCalcAll, mShareCalcCurrentYear;
  private int mCurrentYear;
  private int mAccountNumber = -1;
  private int mCursorRow = -1;
  private List<Transaction> mTransactions;

}
