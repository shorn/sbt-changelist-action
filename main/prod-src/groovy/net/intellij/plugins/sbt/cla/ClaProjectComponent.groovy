package net.intellij.plugins.sbt.cla

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
import net.intellij.plugins.sbt.cla.config.ClaProjectConfigurator
import net.intellij.plugins.sbt.cla.util.ClaUtil
import net.intellij.plugins.sbt.cla.action.ClaActionManager
import com.intellij.openapi.actionSystem.ActionManager
import net.intellij.plugins.sbt.cla.action.ClaCommandExecutionManager

import com.intellij.openapi.application.ApplicationManager

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
  public static final String COMPONENT_NAME = "ClaProjectComponent";

  private final Logger log = Logger.getInstance(getClass())

  // ctor
  Project project
  ClaApplicationComponent appliationComponent

  // project opened
  ClaActionManager actionManager
  ClaCommandExecutionManager executionManager

  // lazy init
  ClaProjectConfigurator configurator


  ClaProjectComponent(Project project) {
    super()
    this.project = project
    appliationComponent =
      ApplicationManager.application.getComponent(ClaApplicationComponent)
  }


  // ---------- ProjectComponent ----------

  void projectOpened() {
    log.debug "projectOpened() ${Thread.currentThread().name} - $project.name"
    actionManager = new ClaActionManager(this, ActionManager.getInstance())
    executionManager = new ClaCommandExecutionManager(this)
    actionManager.addClActions(state.commands)
  }

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
    log.debug "initComponent() ${Thread.currentThread().name} - $project.name [P${project.hashCode()}] [PC${this.hashCode()}]"
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
    return "SBT Change List Action"
  }

  // need to hook up the help up to a wiki topic at some point
  String getHelpTopic() {
    return null;
  }

  Icon getIcon() {
    return ClaUtil.icon32
  }

  /**
   * Checks if the settings in the configuration panel were
   * modified by the user and need to be saved.
   */
  boolean isModified() {
    boolean modified = !configurator.isConfigEquals(state)
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

  /**
   * This method is called by the configurator when a command is edited.
   * Doing all this shennanigans makes me think the command config edit
   * acition should just edit the members and fire an update event or something.
   */
  void commandUpdated(ClaCommand oldCommand, ClaCommand newCommand) {
    executionManager.commandUpdated(oldCommand, newCommand)
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
  String workingDir
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



