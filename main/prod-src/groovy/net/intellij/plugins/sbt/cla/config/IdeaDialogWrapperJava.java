package net.intellij.plugins.sbt.cla.config;

import com.intellij.openapi.ui.DialogWrapper;

import javax.swing.*;

public class IdeaDialogWrapperJava extends DialogWrapper{
  private JComponent preferredFocus;
  private JComponent centerPanel;

  public IdeaDialogWrapperJava() {
    super(true);
  }

  @Override
  protected JComponent createCenterPanel() {
    return centerPanel;
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return preferredFocus;
  }

  public void initPackShow() {
    init();
    pack();
    show();
  }

  public JComponent getPreferredFocus() {
    return preferredFocus;
  }

  public void setPreferredFocus(JComponent preferredFocus) {
    this.preferredFocus = preferredFocus;
  }

  public JComponent getCenterPanel() {
    return centerPanel;
  }

  public void setCenterPanel(JComponent centerPanel) {
    this.centerPanel = centerPanel;
  }
}
