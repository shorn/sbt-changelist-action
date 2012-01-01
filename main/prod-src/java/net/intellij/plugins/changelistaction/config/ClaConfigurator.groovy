package net.intellij.plugins.changelistaction.config

import javax.swing.JPanel
import java.awt.BorderLayout

import com.intellij.ui.table.JBTable
import javax.swing.ListSelectionModel
import ca.odell.glazedlists.gui.TableFormat
import ca.odell.glazedlists.EventList
import ca.odell.glazedlists.swing.EventTableModel
import net.intellij.plugins.changelistaction.util.NoCellFocusRenderer
import ca.odell.glazedlists.GlazedLists
import ca.odell.glazedlists.BasicEventList
import com.intellij.ui.components.JBScrollPane
import javax.swing.JScrollPane
import com.jgoodies.forms.layout.FormLayout
import com.jgoodies.forms.builder.ButtonStackBuilder
import com.jgoodies.forms.layout.CellConstraints
import javax.swing.JButton

import java.awt.event.KeyEvent

import groovy.swing.SwingBuilder
import net.intellij.plugins.changelistaction.ClaState
import net.intellij.plugins.changelistaction.ClaCommand
import net.intellij.plugins.changelistaction.SbtChangelistActionGComponent
import com.intellij.openapi.diagnostic.Logger

class ClaConfigurator {
  private final Logger log = Logger.getInstance(getClass())

  JPanel panel
  TablePanel table;
  SbtChangelistActionGComponent projectComponent

  ClaConfigurator(SbtChangelistActionGComponent projectComponent) {
    this.projectComponent = projectComponent
  }

  ClaConfigurator init() {
    createComponents()
    layoutComponents()
    return this
  }

  private void createComponents() {
    table = new TablePanel(projectComponent).init()
    panel = new JPanel(new BorderLayout())
  }

  private void layoutComponents(){
    panel.add(table.panel, BorderLayout.CENTER)
  }


  void updateConfiguratorFromState(ClaState caState) {
    table.commands.clear();
    table.commands.addAll(caState.commands);
  }
}

class TablePanel {
  private final Logger log = Logger.getInstance(getClass())

  private static String[] columnLabels = [
    "Name"] //, "Class Pattern", "Description"];
  private static String[] columnProps = [
    "name"] //, "classMatchPattern", "description"];

  SbtChangelistActionGComponent projectComponent
  JPanel panel
  EventList<ClaCommand> commands
  JBTable table
  JBScrollPane tableScollPane
  JButton addButton
  JButton editButton
  JButton removeButton
  JButton moveUpButton
  JButton moveDownButton

  TablePanel(SbtChangelistActionGComponent projectComponent) {
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
    buttonBuilder.addGridded(addButton);
    buttonBuilder.addRelatedGap();
    buttonBuilder.addGridded(editButton);
    buttonBuilder.addRelatedGap();
    buttonBuilder.addGridded(removeButton);
    buttonBuilder.addRelatedGap();
    buttonBuilder.addGridded(moveUpButton);
    buttonBuilder.addRelatedGap();
    buttonBuilder.addGridded(moveDownButton);

    panel.setLayout(layout)
    CellConstraints cc = new CellConstraints();
    panel.add(tableScollPane, cc.xy(1, 1));
//    containerPanel.add(buttonBuilder.getPanel(), cc.xy(3, 1));
    panel.add(buttonBuilder.panel, cc.xy(3, 1));


//    panel.add(new JLabel("table stuff goes goes here"))
  }

  private JButton createButton(String text, int key){
    JButton b = new JButton(text);
    b.setMnemonic(key);
    return b;
  }

  private void createComponents() {
    def swing = new SwingBuilder()

    panel = new JPanel()

//    swing.button(){text = "Add..."; mnemonic = KeyEvent.VK_A}
//    swing.button(text:"Add...", mnemonic: KeyEvent.VK_A)

    addButton = createButton("Add...", KeyEvent.VK_A);
    addButton.actionPerformed = {
      log.debug("project: $projectComponent")

      ClaCommandConfigurator editForm =
        new ClaCommandConfigurator().init();

      boolean okButtonPressed =
        editForm.showAsIdeaDialog(this.projectComponent.project, "Add renderer");
      if( okButtonPressed ){
        commands.add([
          name: editForm.name.text,
          command: editForm.command.text,
          options: editForm.options.text] as ClaCommand );
        selectedRow = table.getRowCount()-1;
      }

    }

    editButton = createButton("Edit...", KeyEvent.VK_E);
    removeButton = createButton("Remove", KeyEvent.VK_R);
    moveUpButton = createButton("Move Up", KeyEvent.VK_U);
    moveDownButton = createButton("Move Down", KeyEvent.VK_D);
    [editButton, removeButton, moveUpButton, moveDownButton]*.setEnabled(false)

    table = createTable(commands)
    tableScollPane = new JBScrollPane(
      table,
      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)

    table.model.tableChanged = { adjustButtonEnablement() }
    table.selectionModel.valueChanged =
      { if(!it.valueIsAdjusting) adjustButtonEnablement() }
    table.mouseClicked = { if(it.getClickCount() == 2) editSelectedRow() }

//    table.model.addTableModelListener(
//      { adjustButtonEnablement() } as TableModelListener )
//    table.selectionModel.addListSelectionListener({
//      if(!it.valueIsAdjusting) adjustButtonEnablement()
//    } as ListSelectionListener)


//    table.addMouseListener(
//      new MouseAdapter() {
//        public void mouseClicked(MouseEvent e) {
//          if(e.getClickCount() == 2) editSelectedRow()
//        }
//      }
//    )

  }

  void editSelectedRow() {

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
      [removeButton, editButton, moveUpButton, moveDownButton]*.
        setEnabled(false)
    }
    else {
      // if something is selected, then we can remove or edit it
      [removeButton, editButton]*.setEnabled(true)
      // if it's not the first row, then it can be moved up
      moveUpButton.setEnabled(table.getSelectedRow() != 0)
      // if it's not the last row, then it can be moved down
      moveDownButton.setEnabled(table.getSelectedRow() < table.getRowCount()-1)
    }
  }



}
