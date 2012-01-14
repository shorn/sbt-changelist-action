package net.intellij.plugins.sbt.changelistaction.action

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.diagnostic.Logger
import net.intellij.plugins.sbt.changelistaction.ClaCommand
import net.intellij.plugins.sbt.changelistaction.ClaProjectComponent
import org.apache.commons.lang.StringUtils

class ClaActionManager {
  private final Logger log = Logger.getInstance(getClass())

  ActionManager actionManager
  DefaultActionGroup actionGroup
  ClaProjectComponent projectComponent

  List<ClaCommandPopupMenuAction> actions = []

  ClaActionManager(
    ClaProjectComponent projectComponent,
    ActionManager actionManager)
  {
    this.projectComponent = projectComponent
    this.actionManager = actionManager
    this.actionGroup = getChangesViewMenuActionGroup(this.actionManager)
  }


  static DefaultActionGroup getChangesViewMenuActionGroup(
    ActionManager am)
  {
    return (DefaultActionGroup) am.getAction("ChangesViewPopupMenu")
  }

  /**
   * defines the prefix that all custom actions will use, so we can find them
   * by searching for it.
   *
   * Uses the fully qualified classname to try to ensure uniquenexx of the
   * prefix.
   */
  String getClActionIdPrefix() {
    return projectComponent.project.name
  }

  String formatClActionId(String name){
    return "${getClActionIdPrefix()}.$name"
  }

  /**
   * both unregister and remove from popup menu all clactions
   */
 void removeClActions() {
   actions.each{ ClaCommandPopupMenuAction action ->
     actionGroup.remove(action)
     actionManager.unregisterAction(action.id)
   }

   actions.clear()
  }

  /**
   * Add command actions to changelist popup menu.
   *
   * These actions will show up in teh menu for ALL projects, which
   * is sort of pointless (if you're right-clicking on a CL in one project
   * why would you want to see the actions from a different project?)
   * Thinking about that some more, this is all confused by my need
   * to have different options for the same command across different projects.
   * Maybe I need to make that need go away, or better still, have multuiple
   * types (global and pre-project?), which still leaves me wanting to filter
   * some actions.  Maybe that's it: is there an isVisible method or something?
   */
  void addClActions(List<ClaCommand> commands) {
    for( ClaCommand iCommand : commands ){
      if( StringUtils.isBlank(iCommand.getName()) ){
        log.warn("not adding command with empty name to menu")
        continue
      }

      ClaCommandPopupMenuAction action = new ClaCommandPopupMenuAction(
        projectComponent,
        iCommand,
        formatClActionId(iCommand.name) )

      actionManager.registerAction(action.id, action)
      actionGroup.add(action)

      actions << action
    }
  }

}
