package net.intellij.plugins.sbt.changelistaction.action

import net.intellij.plugins.sbt.changelistaction.ClaProjectComponent
import com.intellij.openapi.wm.ToolWindow
import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.project.Project
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
import net.intellij.plugins.sbt.changelistaction.util.ClaUtil
import com.intellij.execution.filters.TextConsoleBuilder
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.OSProcessHandler
import com.intellij.openapi.diagnostic.Logger
import groovy.swing.SwingBuilder
import com.intellij.util.IJSwingUtilities
import com.intellij.openapi.actionSystem.ActionToolbar

/**
 * The implementation of this class really sucks - I want the GUI widgets
 * to be lazy (not exist until the first time a command is actually executed).
 * The way getConsoleView() and createToolWindow() interact is just wrong.
 * Maybe I should move the laziness - instead of making the GUI widgets
 * lazy on this class, make them eager but make the creationg of this class
 * itself lazy in the projectComponent?
 * Problems with that approach:
 * - handling the clsoing of the window button and disposing of the GUI widgets
 * - handling showing the window
 * - dealing with "re-execute last command" style functionality
 * - I don't want to complicate the project component with details of
 *   executing commands or GUI stuff
 */
class ClaCommandExecutionManager {
  private final Logger log = Logger.getInstance(getClass())
  private static String TOOL_WINDOW_ID = "CLA Console";

  // ctor
  ClaProjectComponent projectComponent

  // lazy
  ConsoleView consoleView;
  ToolWindow toolWindow;

  // state for execute last command toolbar button
  ClaCommandPopupMenuAction lastExecutedCommand


  ClaCommandExecutionManager(ClaProjectComponent projectComponent) {
    this.projectComponent = projectComponent
  }

  ConsoleView getConsoleView(){
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

    return consoleView
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
    actionGroup.add(new AnAction("Invoke changelist action",
      "Invoke previous user action on VCS changelist.",
      ClaUtil.getIcon16())
    {
      public void actionPerformed(AnActionEvent anActionEvent) {
        if( lastExecutedCommand != null ){
          execute(lastExecutedCommand)
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

  /**
   * Executes command and show it in the console.
   */
  public int execute(ClaCommandPopupMenuAction command) {
    lastExecutedCommand = command
    Runtime rt = Runtime.getRuntime();
    log.warn "Invoking $command"

    def stringCommand = command.formatCommandWithOptions()

    try {
      Process proc = rt.exec(stringCommand);
      OSProcessHandler handler = new OSProcessHandler(proc, stringCommand);
      getConsoleView().attachToProcess(handler);
      handler.startNotify();

/*
            // any error message?
            StreamGobbler errorGobbler = new
                    StreamGobbler(proc.getErrorStream(), "ERROR");

            // any output?
            StreamGobbler outputGobbler = new
                    StreamGobbler(proc.getInputStream(), "OUTPUT");

            // kick them off
            errorGobbler.run();
            String out = outputGobbler.run();
*/

      // any error???
      int exitValue = proc.waitFor();
      log.debug("Exit value: " + exitValue);
      return exitValue;
    }
    catch (Exception ex) {
      // and error will actually show up in idea in one of those wierd
      // popup panels, at least the first time
      log.error("Error executing command.", ex);
    }
    return 0;
  }


}
