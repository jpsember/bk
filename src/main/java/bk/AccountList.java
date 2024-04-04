package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.List;

import bk.gen.Account;
import bk.gen.Alignment;
import bk.gen.Column;
import bk.gen.Datatype;

public class AccountList extends LedgerWindow implements ChangeListener {

  public AccountList(AccountListListener listener) {
    changeManager().addListener(this);
    mListener = listener;
    addColumns();
    rebuild();
  }

  private void addColumns() {
    addColumn(Column.newBuilder().name("#").datatype(Datatype.ACCOUNT_NUMBER));
    addColumn(VERT_SEP);
    addColumn(Column.newBuilder().name("Name").datatype(Datatype.TEXT).width(CHARS_ACCOUNT_NAME));
    addColumn(VERT_SEP);
    addColumn(Column.newBuilder().name("Balance").alignment(Alignment.RIGHT).datatype(Datatype.CURRENCY));
  }

  public void rebuild() {
    var currentAccount = getCurrentRow();

    clearEntries();
    var accts = storage().accounts();
    List<Account> sorted = arrayList();
    sorted.addAll(accts.values());
    sorted.sort(ACCOUNT_COMPARATOR);

    for (var t : sorted) {
      List<LedgerField> v = arrayList();
      v.add(new AccountNumberField(t.number()));
      v.add(VERT_SEP_FLD);
      v.add(new AccountNameField(t.name()));
      v.add(VERT_SEP_FLD);
      v.add(new CurrencyField(t.balance()));
      addEntry(v, t);
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

    //    case Character: {
    //      var sum = keyInfo(k);
    //      switch (sum) {
    case ":a":
      mListener.addAccount();
      rebuild();
      handled = true;
      break;
    case ":e":
      if (a != null) {
        mListener.editAccount(a);
        rebuild();
      }
      handled = true;
      break;
    //      }
    //    }
    //      break;
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

}
