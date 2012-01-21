package net.intellij.plugins.sbt.cla.action

import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * represents an invocation of a particular command via an action.
 * The actionEvent data context may indicated mutiple changesets.
 * This is a just quick hack, needs a lot more thought about the
 * best way to represent this.  I don't want to bind the OptionBinding and
 * AnActionEvent classes to gether, but the option binding/execution manager
 * needs access to
 * the context of the command invocation (for getting changelists etc).
 * Needs more thought - just going to ahck something together for the moment
 * and see what emerges.
 */
class ClaActionInvocation {
  ClaCommandPopupMenuAction action
  AnActionEvent actionEvent



}
