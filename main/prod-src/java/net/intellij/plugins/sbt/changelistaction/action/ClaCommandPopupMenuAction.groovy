package net.intellij.plugins.sbt.changelistaction.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

import net.intellij.plugins.sbt.changelistaction.util.ClaUtil
import net.intellij.plugins.sbt.changelistaction.ClaProjectComponent
import net.intellij.plugins.sbt.changelistaction.ClaCommand

class ClaCommandPopupMenuAction extends AnAction {
  ClaProjectComponent projectComponent
  ClaCommand command;
  String id

  ClaCommandPopupMenuAction(
    ClaProjectComponent projectComponent,
    ClaCommand iCommand,
    String id)
  {
    super(
      iCommand.name,
      "",  // description
      ClaUtil.getIcon16() )
    this.projectComponent = projectComponent
    this.command = iCommand
    this.id = id

    // overwrite the description set in the ctor call coz we couldn't call
    // the format method from there
    getTemplatePresentation().setDescription(formatCommandWithOptions());
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    projectComponent.executionManager.execute(formatCommandWithOptions())


//    DataContext dataContext = e.getDataContext();
//    Project project = DataKeys.PROJECT.getData(dataContext);

    // TODO:SBT consider adding an option to "auto-clear" the console
    // before every action

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

  private String formatCommandWithOptions() {
    return "$command.command $command.options"
  }

}
