package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.List;

import bk.gen.Alignment;
import bk.gen.Column;
import bk.gen.Datatype;

@Deprecated
public class FormWindow extends LedgerWindow {

  public FormWindow() {
    todo("highlight only the value field of the current line");
    todo("have cursor keys skip empty fields");
    todo("have blinking cursor on active field somehow");
    loadUtil();
    addColumn(Column.newBuilder().name("Label").datatype(Datatype.TEXT).width(14).alignment(Alignment.RIGHT));
    addColumn(Column.newBuilder().name("Value").datatype(Datatype.TEXT).width(50));
  }

  public void addField(String name, LedgerField value) {
    List<LedgerField> lst = arrayList();
    lst.add(new TextField(name));
    lst.add(value);
    addEntry(lst);
  }

  public void addOkCancel() {
    addField("", EMPTY_FIELD);
     addField("", new TextField("OK"));
    addField("", EMPTY_FIELD);
    addField("", EMPTY_FIELD);
    addField("",new TextField("Cancel"));
  }

  @Override
  public boolean includesHeaderFields() {
    return false;
  }

}
