package bk;

import static js.base.Tools.*;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.junit.Test;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import js.base.DateTimeTools;
import js.file.Files;
import js.testutil.MyTestCase;

public class OpenPDFTest extends MyTestCase {

  @Test
  public void a() {
    if (true)
      return;
    try {
      var sb = new StringBuilder();
      sb.append(name() + ": " + DateTimeTools.humanTimeString() + "\n\n");
      for (int line = 0; line < 300; line++) {
        int maxLen = 110;
        int len = line % (maxLen - 6);
        sb.append(String.format("%4d: ", len));
        for (var i = 0; i < len; i++)
          sb.append((char) ((i % 26) + 'a'));
        sb.append('\n');
      }

      var text = sb.toString();
      log(text);
      Document document = new Document();

      File target = Files.getDesktopFile("experiment.pdf");
      Files.S.deletePeacefully(target);
      boolean TOFILE = false;
      OutputStream bo;
      ByteArrayOutputStream bs = null;
      if (TOFILE)
        bo = new FileOutputStream(target);
      else {
        bs = new ByteArrayOutputStream();
        bo = bs;
      }

      PdfWriter.getInstance(document, bo);
      document.open();
      Font font = new Font(Font.COURIER, 9, Font.NORMAL, Color.BLACK);

      for (String line : split(text, '\n')) {
        log(line);
        var para = new Paragraph(line, font);
        document.add(para);
      }

      document.close();

      if (!TOFILE) {
        Files.S.write(bs.toByteArray(), target);
      }
    } catch (Throwable t) {
      throw asRuntimeException(t);
    }
  }

}
