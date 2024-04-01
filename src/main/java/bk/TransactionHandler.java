package bk;

import bk.gen.Transaction;

public interface TransactionHandler {

  int processEditResult(Transaction.Builder transaction);
}
