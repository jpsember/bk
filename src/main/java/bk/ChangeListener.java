package bk;

import java.util.List;

public interface ChangeListener {
  void dataChanged(List<Integer> modifiedAccountNumbers, List<Long> modifiedTransactionTimestamps);
}
