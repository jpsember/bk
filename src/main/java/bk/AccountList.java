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
    setFooterHeight(3);
    addColumns();
    rebuild();
  }

  @Override
  public void plotFooterContent(int y, int height) {
    var r = Render.SHARED_INSTANCE;
    var b = r.clipBounds();
    int x = b.x;
    plotString("A:add    ret:ledger  T:trans  opt-z:undo  R:rules", x, y);
    plotString("E:edit opt-D:delete  P:print  opt-Z:redo", x, y + 1);
  }

  @Override
  public boolean focusPossible() {
    return true;
  }

  @Override
  public boolean undoEnabled() {
    return true;
  }

  private void addColumns() {
    spaceSeparators();
    addColumn(Column.newBuilder().name("Account").datatype(Datatype.TEXT).width(12).growPct(100));
    addColumn(Column.newBuilder().name("Balance").alignment(Alignment.RIGHT).width(CHARS_CURRENCY)
        .datatype(Datatype.CURRENCY));
  }

  private Account mCurrentAccount;

  public void rebuild() {
    mCurrentAccount = getCurrentRow();

    clearEntries();
    List<Account> sorted = storage().readAllAccounts();
    sorted.sort(ACCOUNT_COMPARATOR);

    for (var a : sorted) {
      openEntry();

      addHint(accountNumberWithNameString(a));
      addHint(a.name());
      addHint(SHORTCUT_TRIE_PREFIX + a.shortcut());
      add(new AccountNameField(a.number(), storage().accountName(a.number())));
      long amount;
      if (hasBudget(a))
        amount = unspentBudget(a);
      else
        amount = a.balance();
      add(new CurrencyField(amount));
      closeEntry(a);
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

      case KeyEvent.RETURN:
        if (a != null) {
          mListener.viewAccount(a);
        }
        break;

      case ":T":
        focusManager().pushAppend(new TransactionLedger(0, mTransListener));
        break;

      case ":R":
        RuleManager.SHARED_INSTANCE.applyRulesToAllTransactions();
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
          PrintManager.SHARED_INSTANCE.printLedger(a);
        }
        break;
      default:
        super.processKeyEvent(k);
        break;
    }
  }

  @Override
  public void dataChanged(List<Integer> accountIds, List<Long> transactionIds) {
    rebuild();
  }

  private AccountListListener mListener;
  private TransactionListener mTransListener;

}
