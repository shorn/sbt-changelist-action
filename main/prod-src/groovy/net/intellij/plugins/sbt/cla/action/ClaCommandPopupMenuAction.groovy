package net.intellij.plugins.sbt.cla.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

import net.intellij.plugins.sbt.cla.util.ClaUtil
import net.intellij.plugins.sbt.cla.ClaProjectComponent
import net.intellij.plugins.sbt.cla.ClaCommand
import com.intellij.openapi.diagnostic.Logger

/**
 * this needs work, need to separate the concept of command, popup and action.
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
    ClaCommand iCommand,
    String id)
  {
    super(
      iCommand.name,
      "",  // description
      ClaUtil.icon16 )
    this.projectComponent = projectComponent
    this.command = iCommand
    this.id = id

    // overwrite the description set in the ctor call coz we couldn't call
    // the format method from there
    // this was in java, maybe groovy makes it doable?
    templatePresentation.description = formatCommandWithOptions()
  }

  void actionPerformed(AnActionEvent e) {
    log.debug "actionPerformed() on thread $ClaUtil.threadName"
    projectComponent.executionManager.execute(this)


//    DataContext dataContext = e.getDataContext();
//    Project project = DataKeys.PROJECT.getData(dataContext);

    // test
//    Messages.showMessageDialog(
//      project,
//      formatCommandWithOptions(),
//      "Information",
//      Messages.getInformationIcon());

    // prod
//    ChangeList[] selectedChangelists = ChangelistUtil.getSelectedChangelists(
//      dataContext);
//    for( ChangeList iChangeList : selectedChangelists ){
//      ChangelistActionComponent.invokeAction(
//        project,
//        iChangeList.getName(),
//        ChangelistUtil.getChangelistFiles(iChangeList),
//        formatCommandWithOptions(),
//        projectComponent.getState().absolutePath,
//        projectComponent.getState().clearConsole);
//    }

  }

  String formatCommandWithOptions() {
    return "$command.command $command.options"
  }

}
