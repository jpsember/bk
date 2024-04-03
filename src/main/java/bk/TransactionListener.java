package bk;

import bk.gen.Transaction;

public interface TransactionListener {

  void editTransaction(int forAccount, Transaction t);

  void addTransaction(int forAccount);
}
