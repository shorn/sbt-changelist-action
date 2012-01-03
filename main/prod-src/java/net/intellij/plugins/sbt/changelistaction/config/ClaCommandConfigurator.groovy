package net.intellij.plugins.sbt.changelistaction.config

import javax.swing.JTextField
import javax.swing.JPanel
import com.jgoodies.forms.layout.FormLayout
import com.jgoodies.forms.layout.CellConstraints
import javax.swing.JLabel

import com.intellij.openapi.ui.TextFieldWithBrowseButton

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.project.Project
import javax.swing.JComponent
import net.intellij.plugins.sbt.changelistaction.ClaCommand

class ClaCommandConfigurator {
  
  JPanel panel
  JTextField name
  JTextField command
  JTextField options
  TextFieldWithBrowseButton commandButton

  ClaCommandConfigurator init(){
    createComponents()
    layoutComponents()
    return this
  }

  void createComponents(){
    panel = new JPanel()
    name = new JTextField()
    command = new JTextField()
    options = new JTextField()
    commandButton = new TextFieldWithBrowseButton(command)
  }

  void layoutComponents(){
    FormLayout layout = new FormLayout(
      "pref, min(200dlu;pref):grow, pref",
      "default, default, default")
    panel.setLayout(layout)
    CellConstraints cc = new CellConstraints()

    panel.add(new JLabel("Name:"), cc.xy(1, 1))
    panel.add(name, cc.xy(2, 1))
    panel.add(new JLabel("Action:"), cc.xy(1, 2))
    panel.add(command, cc.xy(2, 2))
    panel.add(commandButton, cc.xy(3, 2))
    panel.add(new JLabel("Option:"), cc.xy(1, 3))
    panel.add(options, cc.xy(2, 3))
  }

  void updatePanelFieldsFromObject(ClaCommand c) {
    name.text = c.name
    command.text = c.command
    options.text = c.options
  }


  void updateObjectFromPanelFields(ClaCommand c) {
    c.name = name.text
    c.command = command.text
    c.options = options.text
  }

  /**
   * show the edit dialog as a blocking modal popup
   * @return true if user pressed ok button
   */
  boolean showAsIdeaDialog(Project project, String title){
    DialogWrapper dialogWrapper = new IdeaDialogWrapper(project, title);

    dialogWrapper.pack();
    dialogWrapper.show();
    return dialogWrapper.isOK();
  }

  private class IdeaDialogWrapper extends DialogWrapper {

    private IdeaDialogWrapper(Project project, String title) {
      super(project, true);
      this.setTitle(title);
      init();
    }

    protected JComponent createCenterPanel() {
      return getPanel();
    }

    public JComponent getPreferredFocusedComponent() {
      return name;
    }
  }

}
