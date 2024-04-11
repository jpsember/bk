package bk.gen;

public enum UndoState {

  DORMANT, RECORDING, UNDOING, REDOING;

  public static final UndoState DEFAULT_INSTANCE = DORMANT;

}
