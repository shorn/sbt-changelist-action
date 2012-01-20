package net.intellij.plugins.sbt.cla.util


import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * overrides {@link javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, Object, boolean, boolean, int, int)}
 * to hardcode hasFocus to false.
 *
 * Used when you don't want to highlight the currently selected cell in a
 * JTable.
 */
class NoCellFocusRenderer extends DefaultTableCellRenderer {
  @Override
  Component getTableCellRendererComponent(
    JTable table,
    Object value,
    boolean isSelected,
    boolean hasFocus,
    int row,
    int column)
  {
    super.getTableCellRendererComponent(
      table,
      value,
      isSelected,
      false,  // hardcode hasFocus to always be false
      row, column);
  }
}
