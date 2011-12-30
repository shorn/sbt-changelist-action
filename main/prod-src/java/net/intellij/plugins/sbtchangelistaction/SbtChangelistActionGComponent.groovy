package net.intellij.plugins.sbtchangelistaction

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StorageScheme
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@State(
  name = SbtChangelistActionComponent.COMPONENT_NAME,
  storages = [
    @Storage(id = "sbt-changelist-action-default", file = '$PROJECT_FILE$'),
    @Storage(
     id = "sbt-changelist-action-dir",
     file = '$PROJECT_CONFIG_DIR$/sbt-changelist-action.xml',
     scheme = StorageScheme.DIRECTORY_BASED )])
class SbtChangelistActionGComponent
extends SbtChangelistActionComponent
implements PersistentStateComponent<CaState>
{

  private final Logger log = LoggerFactory.getLogger(getClass());

  SbtChangelistActionGComponent(Project project) {
    super(project)
  }


  // ---------- ProjectComponent ----------

  @Override
  void projectOpened() {
    log.debug "projectOpened() - $project.name"
  }

  @Override
  void projectClosed() {
    log.debug "projectClosed() - $project.name"
  }


  // ---------- BaseComponent ----------

  @Override
  void initComponent() {
    log.debug "initComponent() - $project.name"
  }

  @Override
  void disposeComponent() {
    log.debug "disposeComponent() - $project.name"
  }


  // ---------- UnnamedConfigurable ----------

  @Override
  JComponent createComponent() {
    JPanel panel = new JPanel()
    panel.add(new JLabel("not much here yet 2"))

    panel
  }


  // ---------- PersistentStateComponent ----------

  private final CaState state = new CaState();

  @Override
  CaState getState() {
    return state;
  }

  /**
   * Loads state from configuration file.
   */
  @Override
  void loadState(CaState state) {
    XmlSerializerUtil.copyBean(state, this.state)
  }


}


class CaState {
  List<ActionCommand> commands = []
}


class ActionCommand {
  String absolutePath
  String consoleOutput
  String name
  String command
  String options
}



