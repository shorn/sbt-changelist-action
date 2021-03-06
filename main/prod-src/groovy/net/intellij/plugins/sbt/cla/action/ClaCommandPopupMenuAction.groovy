package net.intellij.plugins.sbt.cla.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

import net.intellij.plugins.sbt.cla.util.ClaUtil
import net.intellij.plugins.sbt.cla.ClaProjectComponent
import net.intellij.plugins.sbt.cla.ClaCommand
import com.intellij.openapi.diagnostic.Logger

/**
 * this needs work, need to separate the concept of executable, popup and action.
 * this conflates them all at the moment.  maybe see if i can come up with an
 * elegant solution using groovy mixin/trait thingies?
 */
class ClaCommandPopupMenuAction extends AnAction {
  private final Logger log = Logger.getInstance(getClass())

  ClaProjectComponent projectComponent
  ClaCommand command
  String id

  ClaCommandPopupMenuAction(
    ClaProjectComponent projectComponent,
    ClaCommand command,
    String id)
  {
    super(command.name, "",  ClaUtil.icon16)
    this.projectComponent = projectComponent
    this.command = command
    this.id = id

    // overwrite the description set in the ctor call coz we couldn't call
    // the format method from there
    // this was in java, maybe groovy makes it doable?
    templatePresentation.description = formatCommandWithOptions()
  }

  @Override
  void actionPerformed(AnActionEvent e) {
    log.debug "actionPerformed() on thread $ClaUtil.threadName"
    projectComponent.executionManager.executeAgainstChangeLists(
      new ClaActionInvocation(action: this, actionEvent: e) )
  }

  String formatCommandWithOptions() {
    return "$command.executable $command.options"
  }

  @Override
  void update(AnActionEvent e) {
    e.presentation.text = command.name
  }

}
