package net.intellij.plugins.sbt.cla

import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.diagnostic.Logger

class ClaApplicationComponent implements ApplicationComponent{
  public static final String COMPONENT_NAME = "ClaApplicationComponent"

  private final Logger log = Logger.getInstance(getClass())

  @Override
  void initComponent() {
    log.debug "initComponent() ${Thread.currentThread().name}"
  }

  @Override
  void disposeComponent() {

  }

  @Override
  String getComponentName() {
    return COMPONENT_NAME
  }
}
