package net.intellij.plugins.sbt.cla.action

import net.intellij.plugins.sbt.cla.ClaProjectComponent
import com.intellij.openapi.wm.ToolWindow
import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.wm.ToolWindowManager

import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.util.IconLoader
import javax.swing.JComponent
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import javax.swing.JPanel
import java.awt.BorderLayout
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.content.Content
import com.intellij.openapi.wm.ToolWindowAnchor
import net.intellij.plugins.sbt.cla.util.ClaUtil
import com.intellij.execution.filters.TextConsoleBuilder
import com.intellij.execution.filters.TextConsoleBuilderFactory

import com.intellij.openapi.diagnostic.Logger

import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.execution.ui.ConsoleViewContentType

import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.process.ProcessOutput

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vcs.changes.ChangeList
import net.intellij.plugins.sbt.cla.ClaCommand

/**
 * At the moment, all process write to the same tool window.
 * It probably makes sense to break out seperate executions of an invocation
 * into their own tab or something. The single consoleView is mostly annoying
 * because when you invoke on multiple changesets, seach command is executed
 * in a separate thread and their output ends up being interleaved.
 * I think the consoleView println method is threadsafe (I think) it's just
 * confusing as hell to try and read the output.
 */
class ClaCommandExecutionManager {
  private final Logger log = Logger.getInstance(getClass())
  private static String TOOL_WINDOW_ID = "CLA Console"

  // assigned in ctor
  ClaProjectComponent projectComponent

  // lazy, by init() method first time executeAgainstSelectedChangeLists()
  // is called
  ConsoleView consoleView
  ToolWindow toolWindow

  // saved by executeAgainstSelectedChangeLists()
  ClaActionInvocation lastInvocation


  ClaCommandExecutionManager(ClaProjectComponent projectComponent) {
    this.projectComponent = projectComponent
  }

  ClaCommandExecutionManager init(){
    if( consoleView == null ){
      TextConsoleBuilder builder = TextConsoleBuilderFactory.getInstance().
        createBuilder(projectComponent.project);
      consoleView = builder.getConsole();

      toolWindow = toolWindowMgr.getToolWindow(TOOL_WINDOW_ID)
      // createToolWindow() may call getConsoleView(), so it must
      // not be called until after the console is assigned
      toolWindow = createToolWindow()
      toolWindow.show(null)
    }

    this
  }

  ToolWindowManager getToolWindowMgr(){
    ToolWindowManager.getInstance(projectComponent.project);
  }

  void disposeOfConsole(){
    if( consoleView != null ){
      consoleView.clear();
      consoleView.dispose();
      consoleView = null;
    }

    if( toolWindow != null ){
      toolWindowMgr.unregisterToolWindow(TOOL_WINDOW_ID)
      toolWindow = null;
    }
  }

  private ToolWindow createToolWindow() {
    JComponent toolbarComponent = createToolbar().getComponent();
    JComponent consoleViewComponent = consoleView.getComponent();

    final JPanel panel = new JPanel(new BorderLayout());
    panel.add(toolbarComponent, BorderLayout.WEST);

    ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
    Content content = contentFactory.createContent(
      consoleViewComponent, "", false);
    panel.add(content.getComponent(), BorderLayout.CENTER);

    ToolWindow window = registerToolWindow(toolWindowMgr, panel);
    return window;
  }

  private ActionToolbar createToolbar()
  {
    DefaultActionGroup actionGroup = new DefaultActionGroup();
    actionGroup.add(new AnAction(
      "",
      "Invoke previous user action on VCS changelist.",
      ClaUtil.getIcon16())
    {
      void actionPerformed(AnActionEvent anActionEvent) {
        // not sure if I should replace the invocation event or not,
        // maybe this is what causes the empty changelist problem?
        if (lastInvocation != null) {
          executeAgainstSelectedChangeLists(lastInvocation)
        }
      }

      void update(AnActionEvent e) {
        if( lastInvocation != null ){
          e.presentation.text = "Re-execute $lastInvocation.description"
          e.presentation.enabled = true
        }
        else {
          e.presentation.text = "Re-execute last command"
          e.presentation.enabled = false
        }
      }
    })

    actionGroup.add(
      new AnAction(
        "Clear console",
        "Clear console window.",
        ClaUtil.getIcon("clear.png")) {
        public void actionPerformed(AnActionEvent anActionEvent) {
          consoleView.clear();
        }
      }
    )
    actionGroup.add(new AnAction("Close",
      "Close Changelist Action window.",
      IconLoader.getIcon("/actions/cancel.png")) {
      public void actionPerformed(AnActionEvent anActionEvent) {
        disposeOfConsole();
      }
    });

    return ActionManager.getInstance().createActionToolbar(
      ActionPlaces.UNKNOWN, actionGroup, false)
  }

  private static ToolWindow registerToolWindow(
    ToolWindowManager toolWindowManager,
    final JPanel panel)
  {
    ToolWindow window = toolWindowManager.registerToolWindow(
      TOOL_WINDOW_ID, true, ToolWindowAnchor.BOTTOM)
    ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
    Content content = contentFactory.createContent(panel, "", false);

    content.setCloseable(false);

    window.getContentManager().addContent(content);

    window.setIcon( ClaUtil.getIcon16() );

    return window;
  }

  String getTimestamp(){
    new Date().format('HH:mm:ss')
  }

  void consoleLn(String output){
    consoleView.print("$output\n", ConsoleViewContentType.NORMAL_OUTPUT)
  }
  
  void executeAgainstSelectedChangeLists(ClaActionInvocation invocation){
    if( consoleView == null ){
      init()
    }

    lastInvocation = invocation

    if( invocation.action.command.clearConsole ){
      consoleView.clear()
    }

    ChangeList[] selectedChangelists = invocation.changeLists
    if( selectedChangelists.length == 0 ){
      log.warn "selected changelists collection is empty - how does this happen?"
      return 
    }

    selectedChangelists.each {
      execute(invocation, it)
    }
  }

  private execute(ClaActionInvocation invocation, ChangeList changeList) {
    GeneralCommandLine commandLine = new GeneralCommandLine()
    commandLine.exePath = invocation.action.command.command
    ClaCommandOptionBinding optionBinding =
      new ClaCommandOptionBinding(projectComponent)

    optionBinding.setChangeList(changeList)
    try {
      commandLine.addParameters(
        optionBinding.parseOptions(invocation.action.command.options))
    }
    catch (all) {
      consoleLn(all.toString())
      return
    }
//    commandLine.setWorkDirectory(...)

    consoleLn "[$timestamp] executing $commandLine.commandLineString"
    ApplicationManager.application.executeOnPooledThread {
      try {
        Process process = commandLine.createProcess()

        CapturingProcessHandler processHandler =
          new CapturingProcessHandler(
            process,
            groovy.util.CharsetToolkit.getDefaultSystemCharset());
        consoleView.attachToProcess(processHandler);
        ProcessOutput processOutput = processHandler.runProcess();
        consoleLn "[$timestamp] command returned: $processOutput.exitCode"
      }
      catch (all) {
        consoleLn "[$timestamp] could not execute: $all"
      }
    }
  }

  void commandUpdated(ClaCommand oldCommand, ClaCommand newCommand) {
    if( lastInvocation?.action?.command == oldCommand ){
      lastInvocation.action.command = newCommand
    }
  }
}
