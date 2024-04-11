package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.List;

import bk.gen.Account;
import bk.gen.Transaction;

public class AccountForm extends FormWindow {

  public interface Listener {
    /**
     * Called when user has either modified an account or cancelled edits.
     * Returns modified account, or null if cancel
     */
    void editedAccount(AccountForm form, Account account);
  }

  public static final int TYPE_ADD = 0, TYPE_EDIT = 1;

  public AccountForm(int type, Account account, Listener listener) {
    loadTools();
    account = nullTo(account, Account.DEFAULT_INSTANCE).build();
    mOriginalAccount = account;
    mType = type;
    setSizeChars(12);
    mListener = listener;
    mNumber = validator(ACCOUNT_VALIDATOR).value(account.number()).addField("#");
    mName = validator(ACCOUNT_NAME_VALIDATOR).value(account.name()).fieldWidth(ACCOUNT_NAME_MAX_LENGTH)
        .addField("Name");
    mBudget = validator(BUDGET_VALIDATOR).value(account.budget()).addField("Budget");
    addVertSpace(1);
    addButton("Ok", () -> okHandler());
    addButton("Cancel", () -> cancelHandler());
    addVertSpace(1);
    addMessageLine();
  }

  private void okHandler() {
    String problem = "One or more fields is invalid.";
    var ac = Account.newBuilder();
    if (mNumber.valid() && mName.valid() && mBudget.valid()) {
      ac.number(mNumber.validResult());
      ac.name(mName.validResult());
      ac.budget(mBudget.validResult());
      problem = validateAccount(ac);
    }

    if (problem == null) {

      switch (mType) {
      case TYPE_ADD: {
        var existing = account(ac.number());
        if (existing != null)
          problem = "That account number is taken!";
      }
        break;

      case TYPE_EDIT: {
        var existing = storage().account(ac.number());
        if (existing != null && ac.number() != mOriginalAccount.number())
          problem = "That account number is taken!";
      }
        break;
      }
    }
    if (problem != null) {
      setMessage(problem);
      return;
    }

    Account editedAccount = null;

    var u = UndoManager.SHARED_INSTANCE;
    if (mType == TYPE_ADD) {
      editedAccount = ac;
      u.begin("Add Account", accountNumberWithNameString(ac));
      storage().addOrReplace(ac);
      u.end();
      changeManager().registerModifiedAccount(ac);
    } else {
      u.begin("Modify Account", accountNumberWithNameString(ac));
      // modify a copy of the original account to include the edits
      var orig = mOriginalAccount;
      var mod = orig.build().toBuilder();
      mod.number(ac.number()).name(ac.name()).budget(ac.budget());

      List<Transaction> modTrans = arrayList();
      if (orig.number() != mod.number()) {
        // create modified versions of all transactions from this account
        var origTrans = filterOutGenerated(storage().readTransactionsForAccount(orig.number()));
        for (var t : origTrans) {
          var b = t.toBuilder();
          if (b.debit() == orig.number())
            b.debit(mod.number());
          if (b.credit() == orig.number())
            b.credit(mod.number());
          modTrans.add(b.build());
        }

        // Delete the original transactions
        for (var t : origTrans) {
          storage().deleteTransaction(t);
        }

        // delete the original account
        storage().deleteAccount(orig.number());
      }

      changeManager().registerModifiedAccount(orig);
      editedAccount = mod.build();
      storage().addOrReplace(editedAccount);
      changeManager().registerModifiedAccount(editedAccount);

      // Add the modified transactions.  We've delayed doing this until now,
      // when the modified account (with its possibly new account number) exists.
      for (var t : modTrans) {
        storage().addOrReplace(t);
      }

      u.end();
    }
    mListener.editedAccount(this, editedAccount);
  }

  private void cancelHandler() {
    mListener.editedAccount(this, null);
  }

  private int mType;
  private WidgetWindow mNumber, mName, mBudget;
  private Account mOriginalAccount;
  private Listener mListener;

}
