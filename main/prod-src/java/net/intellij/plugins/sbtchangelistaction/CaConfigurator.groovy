package net.intellij.plugins.sbtchangelistaction

import javax.swing.JPanel
import java.awt.BorderLayout
import com.intellij.openapi.project.Project
import javax.swing.JLabel

class CaConfigurator {

  JPanel panel
  TablePanel table;
  Project project

  CaConfigurator(Project project) {
    this.project = project
  }

  CaConfigurator init() {
    createComponents()
    layoutComponents()
    return this
  }

  private void createComponents(){
    table = new TablePanel().init()
    panel = new JPanel(new BorderLayout())
  }

  private void layoutComponents(){
    panel.add(table.panel, BorderLayout.CENTER);
  }


}

class TablePanel {
  JPanel panel

  TablePanel() {
  }

  TablePanel init() {
    panel = new JPanel()
    panel.add(new JLabel("table stuff goes goes here"))
    return this
  }





}
