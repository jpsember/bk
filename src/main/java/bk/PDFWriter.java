package bk;

import static js.base.Tools.*;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import js.file.Files;

public class PDFWriter {

  public void target(File f) {
    mTarget = f;
  }

  public void content(String content) {
    mContent = content;
  }

  public void close() {
    Files.assertNonEmpty(mTarget, "no target() specified");
    if (mContent == null)
      badState("no content() specified");

    try {
      auxClose();
    } catch (Throwable t) {
      throw asRuntimeException(t);
    }
  }

  private void auxClose() {
    Document document = new Document();

    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    OutputStream outputStream = byteStream;

    PdfWriter.getInstance(document, outputStream);
    document.open();

    var font = font();

    for (String line : split(mContent, '\n')) {
      var para = new Paragraph(line, font);
      document.add(para);
    }

    document.close();

    Files.S.write(byteStream.toByteArray(), mTarget);
  }

  private Font font() {
    if (mFont == null) {
      var fontSize = 8;
      if (false)
        fontSize = tmp;
      mFont = new Font(Font.COURIER, fontSize, Font.NORMAL, Color.BLACK);
    }
    return mFont;
  }

  public static int tmp = 9;
  private File mTarget;
  private String mContent;
  private Font mFont;

}
