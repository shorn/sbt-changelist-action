package net.intellij.plugins.sbt.changelistaction

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StorageScheme
import com.intellij.openapi.project.Project

import javax.swing.JComponent
import com.intellij.openapi.diagnostic.Logger

import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import net.intellij.plugins.sbt.changelistaction.util.ClaUtil
import org.apache.commons.lang.StringUtils
import com.intellij.openapi.actionSystem.AnAction
import javax.swing.Icon

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.components.ProjectComponent
import com.intellij.util.xmlb.XmlSerializerUtil
import net.intellij.plugins.sbt.changelistaction.config.ClaProjectConfigurator

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
  ClaProjectConfigurator configurator;
  Icon pluginIcon;

  ClaProjectComponent(Project project) {
    super()
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

  String getHelpTopic() {
    return null;
  }

  Icon getIcon() {
    if (pluginIcon == null) {
      pluginIcon = ClaUtil.getIcon32();
    }
    return pluginIcon;
  }

  /**
   * Checks if the settings in the configuration panel were
   * modified by the user and need to be saved.
   */
  boolean isModified() {
    boolean modified = !configurator.isConfigEquals(state)
    log.debug("isModified() for $project.name returned $modified")
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

//    ActionManager am = ActionManager.getInstance();
//    DefaultActionGroup changesViewMenuActionGroup =
//      ClaUtil.getChangesViewMenuActionGroup(am)

//    removeAllClActions(
//      am,
//      changesViewMenuActionGroup)
//
//    addClActions(
//      am,
//      changesViewMenuActionGroup,
//      state.commands )

  }

  /**
   * both unregister and remove from popup menu all clactions
   */
  public static void removeAllClActions(
    ActionManager am,
    DefaultActionGroup changesViewPopupMenu)
  {
    am.getActionIds(ClaUtil.getClActionIdPrefix()).each { clActionId ->
      changesViewPopupMenu.remove(am.getAction(clActionId));
      am.unregisterAction(clActionId);
    }
  }

  private void addClActions(
    ActionManager am,
    DefaultActionGroup changesViewPopupMenu,
    List<ClaCommand> commands)
  {
    for( ClaCommand iCommand : commands ){
      if( StringUtils.isBlank(iCommand.getName()) ){
        log.info("not adding command with empty name to menu")
        continue
      }

      AnAction anAction = new ClaCommandPopupMenuAction(this, iCommand)
      am.registerAction(
        ClaUtil.formatClActionId(iCommand.name),
        anAction)
      changesViewPopupMenu.add(anAction)
    }
  }

  void reset() {
    log.debug("reset() called - $project.name")

  }

  void disposeUIResources() {
    log.debug("disposeUIResources() called - $project.name")
  }

  // ---------- PersistentStateComponent ----------

  ClaState state = new ClaState();

  ClaState getState() {
    log.debug "getSate() - $project.name"
    return state;
  }

  /**
   * Loads state from configuration file.
   */
  @Override
  void loadState(ClaState state) {
    log.debug "loadState() - $project.name!"
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



