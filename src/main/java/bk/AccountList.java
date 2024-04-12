package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.List;

import bk.gen.Account;
import bk.gen.Alignment;
import bk.gen.Column;
import bk.gen.Datatype;

public class AccountList extends LedgerWindow implements ChangeListener {

  public AccountList(AccountListListener listener, TransactionListener transListener) {
    loadTools();
    changeManager().addListener(this);
    mListener = listener;
    mTransListener = transListener;
    addColumns();
    rebuild();
  }

  @Override
  public boolean focusPossible() {
    return true;
  }

  @Override
  public boolean undoEnabled() {
    return true;
  }

  private static final boolean MERGED = true;

  private void addColumns() {
    spaceSeparators();
    if (MERGED) {
      addColumn(Column.newBuilder().name("Account").datatype(Datatype.TEXT)
          .width(CHARS_ACCOUNT_NUMBER_AND_NAME).growPct(100));
    } else {
      addColumn(Column.newBuilder().name("#").datatype(Datatype.ACCOUNT_NUMBER));
      addColumn(
          Column.newBuilder().name("Name").datatype(Datatype.TEXT).width(CHARS_ACCOUNT_NAME).growPct(100));
    }
    addColumn(Column.newBuilder().name("Balance").alignment(Alignment.RIGHT).width(CHARS_CURRENCY)
        .datatype(Datatype.CURRENCY));
  }

  private Account mCurrentAccount;

  public void rebuild() {
    mCurrentAccount = getCurrentRow();

    clearEntries();
    List<Account> sorted = storage().readAllAccounts();
    sorted.sort(ACCOUNT_COMPARATOR);

    for (var t : sorted) {
      openEntry();
      if (MERGED) {
        add(new AccountNameField(t.number(), storage().accountName(t.number())));
      } else {
        add(new AccountNumberField(t.number()));
        add(new AccountNameField(t.name()));
      }

      add(new CurrencyField(t.balance()));
      closeEntry(t);
    }
    setCurrentRow(mCurrentAccount);
    repaint();
  }

  @Override
  public int chooseCurrentRow() {
    int bestMatch = 0;
    if (mCurrentAccount != null) {
      int x = size();
      for (int i = 0; i < x; i++) {
        Account a = entry(i);
        if (a.number() >= mCurrentAccount.number()) {
          bestMatch = i;
          break;
        }
      }
    }
    return bestMatch;
  }

  @Override
  public void processKeyEvent(KeyEvent k) {
    Account a = getCurrentRow();

    switch (k.toString()) {

    case ":Q":
      winMgr().quit();
      break;

    case KeyEvent.ENTER:
      if (a != null) {
        mListener.viewAccount(a);
      }
      break;

    case ":T":
      focusManager().pushAppend(new TransactionLedger(0, mTransListener));
      break;

    case KeyEvent.ADD:
      mListener.addAccount();
      rebuild();
      break;

    case KeyEvent.DELETE_ACCOUNT:
      if (a != null) {
        mListener.deleteAccount(a);
        rebuild();
      }
      break;

    case KeyEvent.EDIT:
      if (a != null) {
        mListener.editAccount(a);
        rebuild();
      }
      break;

    case KeyEvent.PRINT:
      if (a != null) {
        PrintManager.SHARED_INSTANCE.pageWidth(110).printLedger(a);
      }
      break;
    default:
      super.processKeyEvent(k);
      break;
    }
  }

  @Override
  public void dataChanged(List<Integer> accountIds, List<Long> transactionIds) {
    discardCachedAccountInfo();
    rebuild();
  }

  private AccountListListener mListener;
  private TransactionListener mTransListener;

}
