package bk;

import bk.gen.Account;

public interface AccountListListener {

  void editAccount(Account account, boolean canRenumber);
  void addAccount();
  void viewAccount(Account account);
  void deleteAccount(Account account);
}
