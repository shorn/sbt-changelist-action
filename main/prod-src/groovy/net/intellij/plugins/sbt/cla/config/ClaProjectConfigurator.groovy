package net.intellij.plugins.sbt.cla.config

import javax.swing.JPanel
import java.awt.BorderLayout

import com.intellij.ui.table.JBTable
import javax.swing.ListSelectionModel
import ca.odell.glazedlists.gui.TableFormat
import ca.odell.glazedlists.EventList
import ca.odell.glazedlists.swing.EventTableModel
import net.intellij.plugins.sbt.cla.util.NoCellFocusRenderer
import ca.odell.glazedlists.GlazedLists
import ca.odell.glazedlists.BasicEventList
import com.intellij.ui.components.JBScrollPane
import javax.swing.JScrollPane
import com.jgoodies.forms.layout.FormLayout
import com.jgoodies.forms.builder.ButtonStackBuilder
import com.jgoodies.forms.layout.CellConstraints
import javax.swing.JButton

import java.awt.event.KeyEvent

import net.intellij.plugins.sbt.cla.ClaState
import net.intellij.plugins.sbt.cla.ClaCommand
import net.intellij.plugins.sbt.cla.ClaProjectComponent
import com.intellij.openapi.diagnostic.Logger

import groovy.swing.SwingBuilder
import com.intellij.openapi.vcs.changes.ChangeListManager
import net.intellij.plugins.sbt.cla.action.ClaCommandOptionBinding
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.JBPopup

class ClaProjectConfigurator {
  private final Logger log = Logger.getInstance(getClass())

  JPanel panel
  TablePanel tablePanel
  ClaProjectComponent projectComponent

  ClaProjectConfigurator(ClaProjectComponent projectComponent) {
    this.projectComponent = projectComponent
  }

  ClaProjectConfigurator init() {
    createComponents()
    layoutComponents()
    return this
  }

  private void createComponents() {
    tablePanel = new TablePanel(projectComponent).init()
    panel = new JPanel(new BorderLayout())
  }

  private void layoutComponents(){
    panel.add(tablePanel.panel, BorderLayout.CENTER)
  }

  void updateConfiguratorFromState(ClaState caState) {
    tablePanel.commands.clear()
    tablePanel.commands.addAll(caState.commands)
  }

  public boolean isConfigEquals(ClaState state) {
    return state.commands.equals(tablePanel.commands)
  }

}

class TablePanel {
  private final Logger log = Logger.getInstance(getClass())

  private static String[] columnLabels = [
    "Name", "Command", "Options"]
  private static String[] columnProps = [
    "name", "executable", "options"]

  ClaProjectComponent projectComponent
  JPanel panel
  EventList<ClaCommand> commands

  JBTable table
  JBScrollPane tableScollPane
  JButton addButton
  JButton editButton
  JButton copyButton
  JButton removeButton
  JButton moveUpButton
  JButton moveDownButton

  TablePanel(ClaProjectComponent projectComponent) {
    this.projectComponent = projectComponent
    commands = GlazedLists.threadSafeList(
      new BasicEventList<ClaCommand>() )
  }

  TablePanel init() {
    createComponents()
    layoutComponents()
    return this
  }

  private layoutComponents() {
    FormLayout layout = new FormLayout(
      "pref:grow, 2dlu, pref, 2dlu",
      "top:pref");

    ButtonStackBuilder buttonBuilder = new ButtonStackBuilder();
    [addButton, editButton, copyButton, removeButton, moveUpButton, moveDownButton].each{
      button ->
        buttonBuilder.addGridded(button);
        buttonBuilder.addRelatedGap();
    }

    panel.setLayout(layout)
    CellConstraints cc = new CellConstraints();
    panel.add(tableScollPane, cc.xy(1, 1));
    panel.add(buttonBuilder.panel, cc.xy(3, 1));


  }

  private void createComponents() {
    SwingBuilder swing = new SwingBuilder()

    panel = new JPanel()

    addButton = swing.button(
      text: "Add...", mnemonic: KeyEvent.VK_A, actionPerformed: {addRow()} )
    editButton = swing.button(
      text: "Edit...",
      mnemonic: KeyEvent.VK_E,
      actionPerformed: {editSelectedRow()} )
    copyButton = swing.button(
      text: "Copy...",
      mnemonic: KeyEvent.VK_C,
      actionPerformed: {copySelectedRow()} )
    removeButton = swing.button(
      text: "Remove",
      mnemonic: KeyEvent.VK_R,
      actionPerformed: {this.removeSelectedRow()} )
    moveUpButton = swing.button(
      text: "Move Up",
      mnemonic: KeyEvent.VK_U,
      actionPerformed: {moveSelectedRow(-1)} )
    moveDownButton = swing.button(
      text: "Move Down",
      mnemonic: KeyEvent.VK_D,
      actionPerformed: {moveSelectedRow(+1)} )
    [editButton, copyButton, removeButton, moveUpButton, moveDownButton]*.
      setEnabled(false)

    table = createTable(commands)
    tableScollPane = new JBScrollPane(
      table,
      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)

    table.model.tableChanged = { adjustButtonEnablement() }
    table.selectionModel.valueChanged =
      { if(!it.valueIsAdjusting) adjustButtonEnablement() }
    table.mouseClicked = { if(it.getClickCount() == 2) editSelectedRow() }
  }

  private moveSelectedRow(int movement) {
    int selectedRow = table.selectedRow
    ClaCommand cmd = commands.remove(selectedRow);
    commands.add(selectedRow + movement, cmd);
    setSelectedRow(selectedRow + movement)
  }

  private addRow() {
    ClaCommandConfigurator editForm = createCommandConfigurator()
    boolean okButtonPressed =
      editForm.showAsIdeaDialog("Add command")

    if (okButtonPressed) {
      ClaCommand cmd = new ClaCommand()
      editForm.updateObjectFromPanelFields(cmd)
      commands.add(cmd)
      selectedRow = table.rowCount - 1
    }
  }

  private copySelectedRow(){
    if (table.selectionModel.isSelectionEmpty()) {
      return
    }

    int selectedRow = table.selectedRow

    ClaCommand oldCmd = commands[selectedRow]
    ClaCommand newCopyCmd = new ClaCommand()
    newCopyCmd.name = oldCmd.name + " copy"
    newCopyCmd.executable = oldCmd.executable
    newCopyCmd.workingDir = oldCmd.workingDir
    newCopyCmd.options = oldCmd.options
    newCopyCmd.clearConsole = oldCmd.clearConsole

    commands.add(selectedRow+1, newCopyCmd);
    setSelectedRow(selectedRow+1)

  }

  private ClaCommandConfigurator createCommandConfigurator() {
    ClaCommandConfigurator editForm =
      new ClaCommandConfigurator(this.projectComponent).init()

    ChangeListManager clMgr =
      ChangeListManager.getInstance(projectComponent.project)

    if( clMgr.defaultChangeList ){
      editForm.optionBinding = new ClaCommandOptionBinding(projectComponent)
      editForm.optionBinding.changeList = clMgr.defaultChangeList
    }

    return editForm
  }

  private removeSelectedRow() {
    if (table.selectionModel.isSelectionEmpty()) {
      return
    }

    Closure onYes = {
      commands.remove(table.selectedRow);

      // select the row below the old removed one
      if (!commands.isEmpty()) {
        int lastRow = table.rowCount - 1
        if (table.selectedRow < lastRow) {
          setSelectedRow(table.selectedRow);
        }
        else {
          setSelectedRow(lastRow);
        }
      }
    }

    JBPopup confirmPopup =
      JBPopupFactory.instance.createConfirmation("delete command?", onYes, 0)
    confirmPopup.showInFocusCenter()

  }

  void editSelectedRow() {
    if( table.selectionModel.selectionEmpty ){
      return
    }

    int selectedRow = table.selectedRow;

    ClaCommandConfigurator editForm = createCommandConfigurator()

    ClaCommand oldCommand = commands.get(selectedRow)
    editForm.updatePanelFieldsFromObject(oldCommand);

    boolean userPressedOk = editForm.showAsIdeaDialog("Edit command");
    if( userPressedOk ){
      // we use a new object so that comparing the list from the project state
      // and the table list  (for "isModified") will come up with false in
      // the case of a single command being edited
      ClaCommand newCommand = new ClaCommand()
      editForm.updateObjectFromPanelFields(newCommand);
      
      if( oldCommand == newCommand ){
        log.info "command was not changed, leaving the old command in place"
      }
      else {
        commands.set(selectedRow, newCommand);
        projectComponent.commandUpdated(oldCommand, newCommand)
      }
    }
  }

  public void setSelectedRow(int selectedRow){
    table.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
  }


  private static JBTable createTable(EventList<ClaCommand> commands){
    TableFormat<ClaCommand> tableFormat =
      GlazedLists.tableFormat(
        ClaCommand.class,
        columnProps,
        columnLabels)

    EventTableModel<ClaCommand> tableModel =
      new EventTableModel<ClaCommand>(commands, tableFormat)

    JBTable table = new JBTable(tableModel)
    table.setDefaultRenderer(String, new NoCellFocusRenderer())

    table.setCellSelectionEnabled(false)
    table.setRowSelectionAllowed(true)
    table.setColumnSelectionAllowed(false)

    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

    return table
  }

  private void adjustButtonEnablement() {
    if( table.selectionModel.isSelectionEmpty() ){
      [removeButton, editButton, copyButton, moveUpButton, moveDownButton]*.
        setEnabled(false)
    }
    else {
      // if something is selected, then we can remove, edit or copy it
      [removeButton, editButton, copyButton]*.setEnabled(true)
      // if it's not the first row, then it can be moved up
      moveUpButton.setEnabled(table.getSelectedRow() != 0)
      // if it's not the last row, then it can be moved down
      moveDownButton.setEnabled(table.getSelectedRow() < table.getRowCount()-1)
    }
  }

}
