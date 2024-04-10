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
    changeManager().addListener(this);
    mListener = listener;
    mTransListener = transListener;
    addColumns();
    rebuild();
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

  public void rebuild() {
    var currentAccount = getCurrentRow();

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
    setCurrentRow(currentAccount);
    repaint();
  }

  @Override
  public void processKeyEvent(KeyEvent k) {
    boolean handled = false;
    Account a = getCurrentRow();

    switch (k.toString()) {

    case KeyEvent.ENTER:
      if (a != null) {
        mListener.viewAccount(a);
        handled = true;
      }
      break;

    case "C:t":
      focusManager().pushAppend(new TransactionLedger(0, mTransListener));
      break;

    case "C:a":
      mListener.addAccount();
      rebuild();
      handled = true;
      break;
    case "C:e":
      if (a != null) {
        mListener.editAccount(a);
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
    rebuild();
    todo("!verify that it attempts to restore cursor to more or less the same location");
  }

  private AccountListListener mListener;
  private TransactionListener mTransListener;

}
