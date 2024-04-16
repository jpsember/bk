package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.junit.Test;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import js.file.Files;
import js.testutil.MyTestCase;

public class OpenPDFTest extends MyTestCase {

  @Test
  public void a() {

    var sb = new StringBuilder();
    for (int line = 0; line < 300; line++) {
      int maxLen = 110;
      int len = line % (maxLen - 6);
      sb.append(String.format("%4d: ", len));
      for (var i = 0; i < len; i++)
        sb.append((char) ((i % 26) + 'a'));
      sb.append('\n');
    }
    var text = sb.toString();
    //    var text = randomText(1000, true);
    log(text);
    Document document = new Document();
    try {
      File target = Files.getDesktopFile("experiment.pdf");
      //var os = new ByteArrayOutputStream();
      Files.S.deletePeacefully(target);
      PdfWriter.getInstance(document, new FileOutputStream(target));
      document.open();
      // font and color settings
      Font font = new Font(Font.COURIER, 12, Font.NORMAL, Color.BLACK);
      for (String line : split(text, '\n')) {
        log(line);
        var para = new Paragraph(line, font);
        log("adding:", para);
        document.add(para);
      }
      //      var s = new String(os.toByteArray());

      //      log(s);
      //      Files.S.writeString(, s);
      //      assertMessage(s);

    } catch (Throwable t) {
      throw asRuntimeException(t);
    }
    document.close();

  }

}
