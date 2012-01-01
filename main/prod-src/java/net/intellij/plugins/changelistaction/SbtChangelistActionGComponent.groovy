package net.intellij.plugins.changelistaction

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StorageScheme
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import javax.swing.JComponent
import com.intellij.openapi.diagnostic.Logger
import net.intellij.plugins.changelistaction.config.ClaConfigurator

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
implements PersistentStateComponent<ClaState>
{
  private final Logger log = Logger.getInstance(getClass())

  Project project
  ClaConfigurator configurator;


  SbtChangelistActionGComponent(Project project) {
    super(project)
    this.project = project
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
    log.debug "initComponent() - $project.name [P${project.hashCode()}] [C${this.hashCode()}]"
  }

  @Override
  void disposeComponent() {
    log.debug "disposeComponent() - $project.name"
  }


  // ---------- UnnamedConfigurable ----------

  @Override
  JComponent createComponent() {
    log.debug "createComponent() - $project.name"

    if( configurator == null ){
      configurator = new ClaConfigurator(this).init()
      configurator.updateConfiguratorFromState(this.state)
    }

    return configurator.panel
  }


  // ---------- PersistentStateComponent ----------

  ClaState state = new ClaState();

  @Override
  ClaState getState() {
    return state;
  }

  /**
   * Loads state from configuration file.
   */
  @Override
  void loadState(ClaState state) {
    XmlSerializerUtil.copyBean(state, this.state)
  }

}


class ClaState {
  List<ClaCommand> commands = []
}


class ClaCommand {
  String absolutePath
  String consoleOutput
  String name
  String command
  String options
}



