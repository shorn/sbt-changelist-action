package net.intellij.plugins.sbt.changelistaction

import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.diagnostic.Logger

class ClaApplicationComponent implements ApplicationComponent{
  public static final String COMPONENT_NAME = "ClaApplicationComponent"

  private final Logger log = Logger.getInstance(getClass())

  @Override
  void initComponent() {
    log.debug "initComponent() in the appComponent"
  }

  @Override
  void disposeComponent() {

  }

  @Override
  String getComponentName() {
    return COMPONENT_NAME
  }
}
