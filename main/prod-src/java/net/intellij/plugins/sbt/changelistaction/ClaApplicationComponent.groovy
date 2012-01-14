package net.intellij.plugins.sbt.changelistaction

import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.wm.ToolWindowManager

class ClaApplicationComponent implements ApplicationComponent{
  public static final String COMPONENT_NAME = "ClaApplicationComponent"

  private final Logger log = Logger.getInstance(getClass())

  @Override
  void initComponent() {
    log.debug "initComponent() ${Thread.currentThread().name}"
    ToolWindowManager toolWindowMgr = ApplicationManager.getApplication().getComponent(ToolWindowManager.class)
    log.debug "initComponent() twm - ${toolWindowMgr}"
  }

  @Override
  void disposeComponent() {

  }

  @Override
  String getComponentName() {
    return COMPONENT_NAME
  }
}
