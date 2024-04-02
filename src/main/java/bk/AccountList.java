package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.List;

import com.googlecode.lanterna.input.KeyStroke;

import bk.gen.Account;
import bk.gen.Column;
import bk.gen.Datatype;

public class AccountList extends LedgerWindow {

  public AccountList() {
    build();
  }

  private void build() {
    final int NAMED_ACCOUNT_WIDTH = 25;
    addColumn(Column.newBuilder().name("#").datatype(Datatype.ACCOUNT_NUMBER));
    addColumn(VERT_SEP);
    addColumn(Column.newBuilder().name("Name").datatype(Datatype.TEXT).width(NAMED_ACCOUNT_WIDTH));
    addColumn(VERT_SEP);
    addColumn(Column.newBuilder().name("Balance").datatype(Datatype.CURRENCY));

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
      addEntry(v);
    }
  }

  @Override
  public void processKeyStroke(KeyStroke k) {
    boolean handled = false;
    switch (k.getKeyType()) {
    case Character: {
      var ch = k.getCharacter();
      switch (ch) {
      case 'a':
        AccountForm.addAccount();
        handled = true;
        break;
      }
    }
      break;
    default:
      break;
    }
    if (!handled)
      super.processKeyStroke(k);
  }

}
