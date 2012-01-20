package net.intellij.plugins.sbt.cla.util

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;

/**
 * Very simple class that allows you to customize the display of an
 * object in a JComboBox without having to write stupid amounts of code.
 * Also adds a generic param to avoid the cast, yay.
 * <p/>
 * Just override getNullString() or getNonNullString() if that's all you
 * want to customize, or getDisplayValue() if you want to do that part
 * yourself.
 *
 * It's criminal that BasicComboBoxRenderer doesn't do this for you.
 */
class SimpleComboRenderer<T> extends BasicComboBoxRenderer {

  /**
   * This is straight copy paste from the parent and then introduces
   * the simple methods to override.  So if the super method ever changes,
   * this method needs to change.  Criminal, I say!
   */
  Component getListCellRendererComponent(
    JList list,
    Object value,
    int index,
    boolean isSelected,
    boolean cellHasFocus)
  {
    if (isSelected) {
      setBackground(list.getSelectionBackground());
      setForeground(list.getSelectionForeground());
    } else {
      setBackground(list.getBackground());
      setForeground(list.getForeground());
    }

    setFont(list.getFont());

    if (value instanceof Icon) {
      setIcon((Icon) value);
    } else {
      setText(getDisplayValue((T) value));
    }
    return this;
  }

  private String getDisplayValue(T value) {
    return (value == null) ? getNullString() : getNonNullString(value);
  }

  public String getNullString(){
    return "";
  }

  public String getNonNullString(T value){
    return value.toString();
  }
}
