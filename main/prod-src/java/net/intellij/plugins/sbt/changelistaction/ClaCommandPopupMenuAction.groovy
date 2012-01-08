package net.intellij.plugins.sbt.changelistaction

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

import net.intellij.plugins.sbt.changelistaction.util.ClaUtil

class ClaCommandPopupMenuAction extends AnAction {
  private ClaProjectComponent projectComponent
  private ClaCommand command;

  ClaCommandPopupMenuAction(
    ClaProjectComponent projectComponent,
    ClaCommand iCommand)
  {
    super(
      iCommand.name,
      "",  // description
      ClaUtil.getIcon16())
    this.projectComponent = projectComponent;
    this.command = iCommand;

    // overwrite the description set in the ctor call coz we couldn't call
    // the format method from there
    getTemplatePresentation().setDescription(formatCommandWithOptions());
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
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
