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
    Font font = new Font(Font.COURIER, 9, Font.NORMAL, Color.BLACK);

    for (String line : split(mContent, '\n')) {
      var para = new Paragraph(line, font);
      document.add(para);
    }

    document.close();

    Files.S.write(byteStream.toByteArray(), mTarget);
  }

  private File mTarget;
  private String mContent;
}
