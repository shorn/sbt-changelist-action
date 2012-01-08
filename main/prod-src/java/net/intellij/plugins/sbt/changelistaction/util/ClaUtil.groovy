package net.intellij.plugins.sbt.changelistaction.util

import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import net.intellij.plugins.sbt.changelistaction.ClaProjectComponent
import javax.swing.Icon
import com.intellij.openapi.util.IconLoader
import javax.swing.ImageIcon

class ClaUtil {
  static final String ICON_LOCATION =
    "/net/intellij/plugins/sbt/changelistaction/icons"
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



}
