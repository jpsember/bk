package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.List;

import bk.gen.ShareCalc;
import bk.gen.Transaction;
import js.base.BaseObject;

public class StockCalculator extends BaseObject {

  public StockCalculator withAccountNumber(int accountNumber) {
    todo("add support for parsing book value from Open statement (=xxxx;YYYY)");
    assertNotCalc();
    mAccountNumber = accountNumber;
    return this;
  }

  public StockCalculator withTransactions(List<Transaction> transactions) {
    assertNotCalc();
    mTransactions = arrayList();
    mTransactions.addAll(transactions);
    mTransactions.sort(TRANSACTION_COMPARATOR);
    return this;
  }

  public StockCalculator withCursor(int cursorRow) {
    assertNotCalc();
    assert (!fiscalYearDefined());
    mCursorRow = cursorRow;
    return this;
  }

  private boolean fiscalYearDefined() {
    return mOpeningDate != 0;
  }

  public StockCalculator withClosingDate(long closeDateSeconds) {
    assertNotCalc();
    assert (!cursorDefined());
    mOpeningDate = closeDateSeconds + 24 * 3600;
    return this;
  }

  private void assertNotCalc() {
    checkState(mShareCalcAll == null, "already calculated");
  }

  private boolean cursorDefined() {
    return mCursorRow >= 0;
  }

  private void calculate() {
    if (mShareCalcAll != null)
      return;

    mShareCalcAll = ShareCalc.newBuilder();
    if (cursorDefined()) {
      mShareCalcToCursor = ShareCalc.newBuilder();
      mCursorYear = ShareCalc.newBuilder();
    } else if (fiscalYearDefined()) {
      mFiscalYear = ShareCalc.newBuilder();
    }

    double capGainPrevYears = 0;

    if (cursorDefined()) {
      Transaction t = mTransactions.get(mCursorRow);
      var d = epochSecondsToLocalDate(t.date());
      mCurrentYear = d.getYear();
      log("current year set to:", mCurrentYear);
    }

    int index = INIT_INDEX;
    for (var t : mTransactions) {
      index++;
      log(VERT_SP, "processing transaction:", INDENT, t);
      updateShare(t, mShareCalcAll);
      if (cursorDefined()) {
        if (index <= mCursorRow)
          updateShare(t, mShareCalcToCursor);
        var year = epochSecondsToLocalDate(t.date()).getYear();
        if (year <= mCurrentYear) {
          updateShare(t, mCursorYear);
        }
        if (year < mCurrentYear)
          capGainPrevYears = mCursorYear.capGain();
      } else if (fiscalYearDefined()) {
        if (t.date() < mOpeningDate)
          updateShare(t, mFiscalYear);
      }
    }

    if (cursorDefined()) {
      mCursorYear.capGain(mCursorYear.capGain() - capGainPrevYears);
    }
    log("currentYear:", INDENT, mCursorYear);
  }

  private void updateShare(Transaction t, ShareCalc.Builder c) {
    //    alertVerbose();
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
    case ASSIGN: {

      // If the amount is zero, it is a stock split (or something), and the book value doesn't change;
      // the parsed book value must be zero.
      //
      // Otherwise, if there is a nonzero book value, update the book value to that amount (this
      // happens in an 'opening' transaction).
      // 
      if (amt == 0) {
        if (si.bookValue() != 0) {
          c.error("did not expect \";<book value>\"");
          break;
        }
      }
      if (si.bookValue() != 0)
        c.bookValue(si.bookValue());
      if (si.shares() < 0) {
        c.error("assign neg shares");
        break;
      }

      c.shares(si.shares());
      log("ASSIGN; parsed info from:", t.description(), INDENT, si, OUTDENT, "calc:", INDENT, c);
    }
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

  public int currentYear() {
    checkState(cursorDefined());
    calculate();
    return mCurrentYear;
  }

  public ShareCalc toCursor() {
    checkState(cursorDefined());
    calculate();
    return mShareCalcToCursor;
  }

  public ShareCalc toFiscalYearEnd() {
    checkState(fiscalYearDefined());
    calculate();
    return mFiscalYear;
  }

  public ShareCalc all() {
    calculate();
    return mShareCalcAll;
  }

  public ShareCalc forCursorYear() {
    checkState(cursorDefined());
    calculate();
    return mCursorYear;
  }

  // For calculating share quantities, cost base, capital gains
  private ShareCalc.Builder mShareCalcToCursor, mShareCalcAll, mCursorYear, mFiscalYear;
  private int mCurrentYear;
  private int mAccountNumber;
  private int mCursorRow = -1;
  private long mOpeningDate;
  private List<Transaction> mTransactions;

}
