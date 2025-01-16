package bk.gen;

public enum UndoState {

  DORMANT, RECORDING, UNDOING, REDOING, INACTIVE;

  public static final UndoState DEFAULT_INSTANCE = DORMANT;

}
