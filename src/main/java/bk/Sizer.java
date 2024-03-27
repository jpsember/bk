package bk;

@Deprecated
class Sizer {
  
  public int widthPct = 100;
  public int heightPct = 100;
  public int widthChars = -1;
  public int heightChars = -1;

  public int getChars(boolean horz) {
    return horz ? widthChars : heightChars;
  }

  public int getPct(boolean horz) {
    return horz ? widthPct : heightPct;
  }
}
