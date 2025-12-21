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
    //alertVerbose();
    account = nullTo(account, Account.DEFAULT_INSTANCE).build();
    mOriginalAccount = account;
    mType = type;
    setSizeChars(12);
    mListener = listener;
    var val = new AccountValidator().withForNewAccount(type == TYPE_ADD);
    mNumber = validator(val).value(account.number()).addField("#");
    mName = validator(ACCOUNT_NAME_VALIDATOR).value(account.name()).fieldWidth(ACCOUNT_NAME_MAX_LENGTH)
        .addField("Name");
    mBudget = validator(BUDGET_VALIDATOR).value(account.budget()).addField("Budget");
    mStock = validator(STOCK_VALIDATOR).value(account.stock()).addField("Stock");
    mShortcut = validator(SHORTCUT_VALIDATOR).value(account.shortcut()).addField("Shortcut");
    addVertSpace(1);
    addButton("Ok", () -> okHandler());
    addButton("Cancel", () -> cancelHandler());
    addVertSpace(1);
    addMessageLine();
  }

  private void okHandler() {
    var ac = Account.newBuilder();

    String problem = "This field is invalid.";

    outer: do {
      if (mNumber.showAlert())
        break;
      if (mName.showAlert())
        break;
      if (mBudget.showAlert())
        break;
      if (mStock.showAlert())
        break;
if (mShortcut.showAlert()) break;

      ac.number(mNumber.validResult());
      ac.name(mName.validResult());
      ac.budget(mBudget.validResult());
      ac.stock(mStock.validResult());
      ac.shortcut(mShortcut.validResult());

      switch (mType) {
      case TYPE_ADD: {
        if (accountExists(ac.number())) {
          problem = "This account number is taken!";
          break outer;
        }
      }
        break;

      case TYPE_EDIT: {
        var existing = storage().account(ac.number());
        if (existing != null && ac.number() != mOriginalAccount.number()) {
          problem = "This account number is taken!";
          break outer;
        }

        todo("fn to check if shortcut taken");
        var prob2 =  checkDupShortcut(ac);
        if (prob2 != null) {
          problem = prob2;
          break outer;
        }

      }
        break;
      }
      problem = null;
    } while (false);

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

      // Copy the fields that we wish to preserve from the old account to the new one
      var orig = mOriginalAccount;
      ac.balance(orig.balance());
      var mod = ac;

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

  private String checkDupShortcut(Account a) {
    String prob = null;
    var sc = a.shortcut();
    if (!sc.isEmpty()) {

      todo("look through all the accounts looking for duplicate shortcut: "+sc);
    }

    return prob;
  }

  private void cancelHandler() {
    mListener.editedAccount(this, null);
  }

  private int mType;
  private WidgetWindow mNumber, mName, mBudget, mStock, mShortcut;
  private Account mOriginalAccount;
  private Listener mListener;

}
