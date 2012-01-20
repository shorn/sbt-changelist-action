package net.intellij.plugins.sbt.cla.action

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.diagnostic.Logger
import net.intellij.plugins.sbt.cla.ClaCommand
import net.intellij.plugins.sbt.cla.ClaProjectComponent
import org.apache.commons.lang.StringUtils
import com.intellij.openapi.actionSystem.AnActionEvent

import com.intellij.openapi.actionSystem.PlatformDataKeys

class ClaActionManager {
  private final Logger log = Logger.getInstance(getClass())

  ActionManager actionManager
  DefaultActionGroup actionGroup
  ClaProjectComponent projectComponent

//  List<ClaCommandPopupMenuAction> actions = []

  ClaActionManager(
    ClaProjectComponent projectComponent,
    ActionManager actionManager)
  {
    this.projectComponent = projectComponent
    this.actionManager = actionManager
    this.actionGroup = new DefaultActionGroup(){
      void update(AnActionEvent e) {
        e.presentation.visible = 
          e.getData(PlatformDataKeys.PROJECT) == projectComponent.project
        e.presentation.enabled = e.presentation.visible
      }
    }
    getChangesViewMenuActionGroup(this.actionManager).add(actionGroup)
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
   actionGroup.childActionsOrStubs.each{ ClaCommandPopupMenuAction action ->
     actionManager.unregisterAction(action.id)
   }
   actionGroup.removeAll()

//   actions.each{ action ->
//     actionGroup.remove(action)
//     actionManager.unregisterAction(action.id)
//   }
//
//   actions.clear()
  }

  void addClActions(List<ClaCommand> commands) {
    commands.each{ command ->
      if( StringUtils.isBlank(command.getName()) ){
        log.warn("not adding command with empty name to menu")
        return
      }

      ClaCommandPopupMenuAction action = new ClaCommandPopupMenuAction(
        projectComponent,
        command,
        formatClActionId(command.name) )

      actionManager.registerAction(action.id, action)
      actionGroup.add(action)

//      actionGroup.
//      actions << action
    }
  }

}
