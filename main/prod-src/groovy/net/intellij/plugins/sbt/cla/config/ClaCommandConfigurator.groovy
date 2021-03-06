package net.intellij.plugins.sbt.cla.config

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.popup.ComponentPopupBuilder
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.UIUtil
import com.jgoodies.forms.layout.CellConstraints
import com.jgoodies.forms.layout.FormLayout
import groovy.swing.SwingBuilder
import javax.swing.JButton
import javax.swing.JCheckBox

import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.JTextField
import net.intellij.plugins.sbt.cla.ClaCommand
import net.intellij.plugins.sbt.cla.ClaProjectComponent
import net.intellij.plugins.sbt.cla.action.ClaCommandOptionBinding
import net.intellij.plugins.sbt.cla.util.ClaUtil

import java.awt.Dimension
import com.intellij.openapi.util.SystemInfo

/**
 * could use some validation
 * - change the help icon to an eye, and make it red if error when parsing :)
 * - maybe popup the error as a tooltip when tabbing out?
 */
class ClaCommandConfigurator {
  private final Logger log = Logger.getInstance(getClass())

  ClaProjectComponent projectComponent

  /**
   * if this is null, the optionHelperButton won't be enabled -
   * it's up to the caller to set this to something that makes sense.
   */
  ClaCommandOptionBinding optionBinding


  JPanel panel
  JTextField name
  JTextField command
  JTextField workingDir
  JTextArea options

  TextFieldWithBrowseButton workingDirButton
  TextFieldWithBrowseButton commandButton
  JButton optionHelperButton

  JCheckBox console

  ClaCommandConfigurator(ClaProjectComponent projectComponent){
    this.projectComponent = projectComponent;
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
    workingDir = new JTextField()
    options = new JTextArea()

    commandButton = new TextFieldWithBrowseButton(command)
    commandButton.addBrowseFolderListener(
      "title",
      "desc",
      projectComponent.project,
      FileChooserDescriptorFactory.createSingleLocalFileDescriptor() )
    workingDirButton = new TextFieldWithBrowseButton(workingDir)
    workingDirButton.addBrowseFolderListener(
      "title",
      "desc",
      projectComponent.project,
      FileChooserDescriptorFactory.createSingleFolderDescriptor())
    optionHelperButton = new SwingBuilder().button(
      icon: ClaUtil.icon16,
      actionPerformed: {optionHelperPressed()}
    )
    optionHelperButton.enabled = optionBinding != null

    // button was too wide on windows
    if( SystemInfo.isWindows ){
      optionHelperButton.preferredSize =
        new Dimension(ClaUtil.icon16.iconWidth+4, ClaUtil.icon16.iconHeight+4)
    }

    console = new JCheckBox(selected: true)
  }

  /**
   * The "600dlu" max for the middle column could probably use a better value,
   * maybe make sure the dialog is not wider than the display or something?
   * Note that the [X, Y, Z] colspec requires a recent version of formlayout
   * - more recent than the one packaged with IDEA 11.x at least.
   */
  void layoutComponents(){
    FormLayout layout = new FormLayout(
      "right:pref, [200dlu,pref,600dlu]:grow, pref",
      "default, default, default, fill:[50dlu,default,200dlu]:grow, default")
    panel.setLayout(layout)
    CellConstraints cc = new CellConstraints()

    // more than 4 or 5 rows is too many, start using a builder pattern
    panel.add(new JLabel("Name:"), cc.xy(1, 1))
    panel.add(name, cc.xy(2, 1))

    panel.add(new JLabel("Command:"), cc.xy(1, 2))
    panel.add(command, cc.xy(2, 2))

    panel.add(commandButton, cc.xy(3, 2))
    panel.add(new JLabel("Working Directory:"), cc.xy(1, 3))

    panel.add(workingDir, cc.xy(2, 3))
    panel.add(workingDirButton, cc.xy(3, 3))

    panel.add(new JLabel("Options:"), cc.xy(1, 4, "right, top"))
    panel.add(new JBScrollPane(options), cc.xy(2, 4))
    panel.add(optionHelperButton, cc.xy(3, 4, "left, bottom"))

    panel.add(
      new JLabel(
        text: "Clear console:",
        toolTipText: "Clear the console before the executable is executed" ),
      cc.xy(1, 5) )
    panel.add(console, cc.xy(2, 5))
  }

  /**
   * copies data from the given command into the input fields.
   */
  void updatePanelFieldsFromObject(ClaCommand c) {
    name.text = c.name
    command.text = c.executable
    workingDir.text = c.workingDir
    options.text = c.options
    console.selected = c.clearConsole
  }

  /**
   * copies data from the input fields into a command object
   */
  void updateObjectFromPanelFields(ClaCommand c) {
    c.name = name.text
    c.executable = command.text
    c.workingDir = workingDir.text
    c.options = options.text
    c.clearConsole = console.selected
  }

  void setOptionBinding(ClaCommandOptionBinding optionBinding) {
    this.optionBinding = optionBinding
  }

  void optionHelperPressed() {
    log.debug "optionHelperPressed"

    List result = null
    def error = null
    try {
      result = getOptionBinding().parseOptions(options.text)
    }
    catch( all ){
      error = all
    }

    StringBuilder doco = new StringBuilder()
    getOptionBinding().optionsDocs.each{ name, doc ->
      doco << "$name - $doc <br>"
    }
    doco << "<hr>"


    log.debug("options eval: $result|$error")
    String content
    if( result ){
      content = UIUtil.toHtml("$doco options: $result")
    }
    else {
      content = UIUtil.toHtml("$doco error: $error")
    }
    JPanel contentPanel = new JPanel()
    contentPanel.add( new JLabel(content) )

    ComponentPopupBuilder builder = JBPopupFactory.getInstance().
      createComponentPopupBuilder(contentPanel, contentPanel)
    builder.setProject(projectComponent.project).
      createPopup().showUnderneathOf(optionHelperButton)
  }

  /**
   * show the edit dialog as a blocking modal popup
   * @return true if user pressed ok button
   */
  boolean showAsIdeaDialog(String title){

    // I don't want to think about the "right" time to do this, so I'm just
    // doing it here at the last possible moment :)
    configureOptionHelper()

    DialogWrapper dialogWrapper = new IdeaDialogWrapperJava(
      centerPanel: panel, preferredFocus: name, resizable: true)
    dialogWrapper.initPackShow()

    return dialogWrapper.isOK()
  }

  private void configureOptionHelper() {
    if (optionBinding) {
      optionHelperButton.enabled = true
      optionHelperButton.toolTipText = "display a panel that shows what command would be executed, given that it was executed on the '${optionBinding?.changeList?.name}' changelist"
    }
    else {
      optionHelperButton.enabled = false
      optionHelperButton.toolTipText = "disabled because there is no currently active changelist to try the command out on"
    }
  }

}
