package net.intellij.plugins.sbt.changelistaction.util

import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import net.intellij.plugins.sbt.changelistaction.SbtChangelistActionGComponent
import javax.swing.Icon
import com.intellij.openapi.util.IconLoader

class ClaUtil {
  static final String ICON_LOCATION =
    "/net/intellij/plugins/changelistaction/icons"

  static DefaultActionGroup getChangesViewMenuActionGroup(
    ActionManager am)
  {
    return (DefaultActionGroup) am.getAction("ChangesViewPopupMenu")
  }

  /**
   * defines the prefix that all custom actions will use, so we can find them
   * by searching for it.
   *
   * Uses the fully qualified classname to try to ensure uniquenexx of the
   * prefix.
   */
  static String getClActionIdPrefix() {
    return SbtChangelistActionGComponent.name
  }

  public static String formatClActionId(String name){
    return "${getClActionIdPrefix()}.$name"
  }

  // TODO:SBT think about moving caching from project component into here
  // and caching the 16 icon especially, which gets loaded for each
  // action at the moment (check that there's not already some cleverness
  // going on inside IconLoader though.
  static Icon getIcon32() {
      return IconLoader.getIcon("$ICON_LOCATION/frog32.png");
  }

  static Icon getIcon16() {
    return IconLoader.getIcon("$ICON_LOCATION/frog16.png")
  }



}
