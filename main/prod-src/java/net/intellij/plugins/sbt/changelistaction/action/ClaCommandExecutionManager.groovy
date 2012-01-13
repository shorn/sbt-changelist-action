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

class ClaCommandExecutionManager {
  private final Logger log = Logger.getInstance(getClass())
  private static String TOOL_WINDOW_ID = "CLA Console";

  ClaProjectComponent projectComponent

  ToolWindow toolWindow;
  ConsoleView consoleView;

  ClaCommandExecutionManager(ClaProjectComponent projectComponent) {
    log.warn "ClaCommandExecutionManager.ctor - ${Thread.currentThread().name} "
    this.projectComponent = projectComponent

//    IJSwingUtilities.invoke{
//      log.warn "doLater - ${Thread.currentThread().name} "
//    }
  }

  ConsoleView getConsoleView(){
    if( consoleView == null ){
      ToolWindowManager manager =
        ToolWindowManager.getInstance(projectComponent.project);
      toolWindow = manager.getToolWindow(TOOL_WINDOW_ID);
      toolWindow = createToolWindow(manager, projectComponent.project);

      TextConsoleBuilder builder =
        TextConsoleBuilderFactory.getInstance().createBuilder(
          projectComponent.project);
      consoleView = builder.getConsole();
    }
    return consoleView
  }

  void disposeOfConsole(){
    if( consoleView != null ){
      consoleView.dispose();
      consoleView = null;
    }
  }

  private ToolWindow createToolWindow(
    ToolWindowManager toolWindowManager,
    Project project)
  {
    DefaultActionGroup actionGroup = new DefaultActionGroup();
//    actionGroup.add(new AnAction("Invoke changelist action",
//      "Invoke previous user action on VCS changelist.",
//      ClaUtil.getIcon16())
//    {
//      public void actionPerformed(AnActionEvent anActionEvent) {
//        execute(command, project)
//      }
//    })

    actionGroup.add(
      new AnAction(
        "Clear console",
        "Clear console window.",
        ClaUtil.getIcon("clear.png") )
      {
        public void actionPerformed(AnActionEvent anActionEvent) {
          getConsoleView().clear();
        }
      }
    )
    actionGroup.add(new AnAction("Close",
      "Close Changelist Action window.",
      IconLoader.getIcon("/actions/cancel.png") )
    {
      public void actionPerformed(AnActionEvent anActionEvent) {
        getConsoleView().clear();
        toolWindowManager.unregisterToolWindow(TOOL_WINDOW_ID);
      }
    });

    JComponent toolbar = ActionManager.getInstance().createActionToolbar(
      ActionPlaces.UNKNOWN, actionGroup, false).getComponent();


    final JPanel panel = new JPanel(new BorderLayout());
    panel.add(toolbar, BorderLayout.WEST);

    ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
    // t`odo:sbt see if "consoleView" will actually call the getter?
    JComponent viewComponent = getConsoleView().getComponent();
    Content content =
      contentFactory.createContent(viewComponent, "", false);

    panel.add(content.getComponent(), BorderLayout.CENTER);

    ToolWindow window = registerToolWindow(toolWindowManager, panel);

    window.show(null)
    return window;
  }

  private static ToolWindow registerToolWindow(
    final ToolWindowManager
    toolWindowManager,
    final JPanel panel)
  {
    final ToolWindow window = toolWindowManager.registerToolWindow(
      TOOL_WINDOW_ID,
      true,
      ToolWindowAnchor.BOTTOM);
    final ContentFactory contentFactory =
      ContentFactory.SERVICE.getInstance();

    final Content content = contentFactory.createContent(panel, "", false);

    content.setCloseable(false);

    window.getContentManager().addContent(content);

    window.setIcon( ClaUtil.getIcon16() );

    return window;
  }

  /**
   * Executes command and show it in the console.
   */
  public int execute(String command) {
    try {
      Runtime rt = Runtime.getRuntime();
      log.warn "Invoking $command"

      Process proc = rt.exec(command);
      OSProcessHandler handler = new OSProcessHandler(proc, command);
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
