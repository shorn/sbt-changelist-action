package net.intellij.plugins.sbt.changelistaction

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StorageScheme
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import javax.swing.Icon
import javax.swing.JComponent
import net.intellij.plugins.sbt.changelistaction.config.ClaProjectConfigurator
import net.intellij.plugins.sbt.changelistaction.util.ClaUtil
import net.intellij.plugins.sbt.changelistaction.action.ClaActionManager

@State(
  name = ClaProjectComponent.COMPONENT_NAME,
  storages = [
    @Storage(id = "sbt-changelist-action-default", file = '$PROJECT_FILE$'),
    @Storage(
     id = "sbt-changelist-action-dir",
     file = '$PROJECT_CONFIG_DIR$/sbt-changelist-action.xml',
     scheme = StorageScheme.DIRECTORY_BASED )])
class ClaProjectComponent
implements
  Configurable,
  ProjectComponent,
  PersistentStateComponent<ClaState>
{
  public static final String COMPONENT_NAME = "SBT VCS Changelist Action";

  private final Logger log = Logger.getInstance(getClass())

  Project project
  ClaActionManager actionManager

  ClaProjectConfigurator configurator


  ClaProjectComponent(Project project) {
    super()
    this.project = project
    this.actionManager = new ClaActionManager(this)
  }


  // ---------- ProjectComponent ----------

  @Override
  void projectOpened() {
    log.debug "projectOpened() - $project.name"
    actionManager.addClActions(state.commands)
  }

  @Override
  void projectClosed() {
    log.debug "projectClosed() - $project.name"
    actionManager.removeClActions()
  }

  // ---------- NamedComponent ----------

  String getComponentName() {
    return "sbt-changelist-action project component"
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
      configurator = new ClaProjectConfigurator(this).init()
      configurator.updateConfiguratorFromState(this.state)
    }

    return configurator.panel
  }


  // ---------- Configurable ----------

  String getDisplayName() {
    return COMPONENT_NAME
  }

  // need to hook up the help up to a wiki topic at some point
  String getHelpTopic() {
    return null;
  }

  Icon getIcon() {
    return ClaUtil.getIcon32();
  }

  /**
   * Checks if the settings in the configuration panel were
   * modified by the user and need to be saved.
   */
  boolean isModified() {
    boolean modified = !configurator.isConfigEquals(state)
    log.debug "isModified() for $project.name returned $modified"
    return modified
  }

  /**
   * Store the settings from configurable to other components.
   * Repaints all editors.
   */
  void apply() throws ConfigurationException {
    log.debug("apply() called - $project.name")
    state.commands.clear()
    state.commands.addAll(configurator.tablePanel.commands)

    actionManager.removeClActions()
    actionManager.addClActions(state.commands)
  }

  void reset() {
    log.debug("reset() called - $project.name")
    if( configurator == null ){
      log.warn "reset() called when not configurator has been created, how does that happen?"
    }
    configurator.updateConfiguratorFromState(this.state)
  }

  void disposeUIResources() {
    log.debug("disposeUIResources() called - $project.name")
  }

  // ---------- PersistentStateComponent ----------

  ClaState state = new ClaState();

  ClaState getState() {
    return state
  }

  /**
   * Loads state from configuration file.
   */
  @Override
  void loadState(ClaState state) {
    log.debug "loadState() - $project.name"
    XmlSerializerUtil.copyBean(state, this.state)
  }

}


class ClaState {
  List<ClaCommand> commands = []
}


class ClaCommand {
  PathFormat filenames
  boolean clearConsole
  String name
  String command
  String options

  static enum PathFormat {
    ABSOLUTE("Absolute"),
    RELATIVE("Relative")

    String description

    PathFormat(String description) {
      this.description = description
    }
  }
}



