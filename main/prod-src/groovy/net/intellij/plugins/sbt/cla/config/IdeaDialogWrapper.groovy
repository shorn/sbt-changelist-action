package net.intellij.plugins.sbt.cla.config

import com.intellij.openapi.ui.DialogWrapper
import javax.swing.JComponent

class IdeaDialogWrapper extends DialogWrapper {
  JComponent preferredFocus
  JComponent centerPanel

  public IdeaDialogWrapper() {
    super(true)
  }

  @Override
  protected JComponent createCenterPanel() {
    return centerPanel
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return preferredFocus
  }

  void initPackShow() {
    init()
    pack()
    show()
  }
}


