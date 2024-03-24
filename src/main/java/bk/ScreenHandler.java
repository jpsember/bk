package bk;

import com.googlecode.lanterna.input.KeyStroke;

import js.geometry.IPoint;

public interface ScreenHandler {
  
  void processKey(KeyStroke keyStroke);

  void repaint();
  
  void processNewSize(IPoint size);
}
