package net.intellij.plugins.sbt.cla.util

import javax.swing.Icon

import javax.swing.ImageIcon

class ClaUtil {
  static final String ICON_LOCATION =
    "/net/intellij/plugins/sbt/cla/icons"
  static Icon icon16;
  static Icon icon32;

  static final Map<String, Icon> icons = [:]
  
  static Icon getIcon(String name){
    if( icons[name] == null ){
      icons[name] = new ImageIcon(ClaUtil.getResource("$ICON_LOCATION/$name"))
    }

    return icons[name]
  }


  static Icon getIcon32() {
    return getIcon("frog32.png")
  }

  static Icon getIcon16() {
    return getIcon("frog16.png")
  }

  static String getThreadName(){
    return Thread.currentThread().name
  }

}
