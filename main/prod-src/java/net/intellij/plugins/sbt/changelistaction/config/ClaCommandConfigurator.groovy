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

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import javax.swing.JCheckBox
import javax.swing.JComboBox
import net.intellij.plugins.sbt.changelistaction.util.SimpleComboRenderer
import com.intellij.openapi.diagnostic.Logger

class ClaCommandConfigurator {
  private final Logger log = Logger.getInstance(getClass())

  Project project

  JPanel panel
  JTextField name
  JTextField command
  JTextField options
  TextFieldWithBrowseButton commandButton
  JComboBox filePaths
  JCheckBox console

  ClaCommandConfigurator(Project project){
    this.project = project;
  }
  
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
    commandButton.addBrowseFolderListener(
      "thetitle",
      "thedesc",
      project,
      FileChooserDescriptorFactory.createSingleFileDescriptor(null))

    filePaths = new JComboBox(ClaCommand.PathFormat.enumConstants)
    
    filePaths.setRenderer(new SimpleComboRenderer<ClaCommand.PathFormat>(){
      String getNonNullString(ClaCommand.PathFormat value) {
        return value.description
      }
    })

    console = new JCheckBox(selected: true)
  }

  /**
   * The "600dlu" max for the middle column could probably use a better value,
   * maybe make sure the dialog is not wider than the display or something?
   * Note that the [X, Y, Z] colspec requires a recent version of formlayout
   * - more recent than the one packaged with IDEA at least.
   */
  void layoutComponents(){
    FormLayout layout = new FormLayout(
      "pref, [200dlu,pref,600dlu]:grow, pref",
      "default, default, default, default, default")
    panel.setLayout(layout)
    CellConstraints cc = new CellConstraints()

    panel.add(new JLabel("Name:"), cc.xy(1, 1))
    panel.add(name, cc.xy(2, 1))
    panel.add(new JLabel("Action:"), cc.xy(1, 2))
    panel.add(command, cc.xy(2, 2))
    panel.add(commandButton, cc.xy(3, 2))
    panel.add(new JLabel("Option:"), cc.xy(1, 3))
    panel.add(options, cc.xy(2, 3))
    panel.add(new JLabel("Filenames:"), cc.xy(1, 4))
    panel.add(filePaths, cc.xy(2, 4))
    panel.add(
      new JLabel(
        text: "Clear console:",
        toolTipText: "Clear the console before the command is executed" ),
      cc.xy(1, 5) )
    panel.add(console, cc.xy(2, 5))
  }

  void updatePanelFieldsFromObject(ClaCommand c) {
    name.text = c.name
    command.text = c.command
    options.text = c.options
    filePaths.selectedItem = c.filenames
    console.selected = c.clearConsole
  }


  void updateObjectFromPanelFields(ClaCommand c) {
    c.name = name.text
    c.command = command.text
    c.options = options.text
    c.filenames = filePaths.selectedItem as ClaCommand.PathFormat
    c.clearConsole = console.selected
  }

  /**
   * show the edit dialog as a blocking modal popup
   * @return true if user pressed ok button
   */
  boolean showAsIdeaDialog(String title){
    DialogWrapper dialogWrapper = new IdeaDialogWrapper(project, title);

    dialogWrapper.setResizable(true)

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
