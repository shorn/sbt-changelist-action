package net.intellij.plugins.sbtchangelistaction

import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StorageScheme
import com.intellij.openapi.project.Project
import com.intellij.openapi.diagnostic.Logger
import org.slf4j.Logger;
import org.slf4j.LoggerFactory
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JLabel;


class SbtChangelistActionGComponent extends SbtChangelistActionComponent {

  private final Logger log = LoggerFactory.getLogger(getClass());

  public SbtChangelistActionGComponent(Project project) {
    super(project)
  }

  @Override
  void projectOpened() {
    log.debug "projectOpened() - $project.name"
  }

  @Override
  void projectClosed() {
    log.debug "projectClosed() - $project.name"
  }

  @Override
  void initComponent() {
    log.debug "initComponent() - $project.name"
  }

  @Override
  void disposeComponent() {
    log.debug "disposeComponent() - $project.name"
  }

  @Override
  public JComponent createComponent() {
    JPanel panel = new JPanel();
    panel.add(new JLabel("not much here yet 2"));

    return panel;
  }

}
