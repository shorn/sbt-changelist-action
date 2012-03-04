package net.intellij.plugins.sbt.cla.action

import com.intellij.openapi.diagnostic.Logger
import groovy.transform.InheritConstructors

import java.lang.annotation.Retention
import java.lang.annotation.Target
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.ElementType
import java.lang.reflect.Method

import net.intellij.plugins.sbt.cla.ClaProjectComponent

import com.intellij.openapi.vcs.changes.ChangeList

import net.intellij.plugins.sbt.cla.util.ClaUtil
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ContentRevision
import static com.intellij.openapi.vcs.changes.Change.Type.DELETED

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface OptionBinding{
  /** expected to be some doco of the option */
  String value()
}

@InheritConstructors
class OptionParsingException extends RuntimeException{ }


class ClaCommandOptionBinding {
  private final Logger log = Logger.getInstance(getClass())

  ClaProjectComponent projectComponent
  ChangeList changeList

  ClaCommandOptionBinding(ClaProjectComponent projectComponent) {
    this.projectComponent = projectComponent
  }
  
  /**
   * Takes the given "options script" and executes it to return a list of
   * Stirng parameters that will be passed to the command to execute.
   *
   * This method doesn't do any exception handling, especially
   * {@link GroovyShell#evaluate} exceptions are propogated.
   *
   * Note also that the actual results of the script execution will be passed
   * through the {@link #flattenEvalResult} method in order to turn anything
   * that's not a string into a string.
   *
   * @throws OptionParsingException if the expression doesn't evaluate
   * to a list
   */
  List<String> parseOptions(String options){
    // override binding.getVariable() to allow unqualifed access to the
    // whatever properties we define on the enclosing ClaCommandOptionBinding
    // class with the OptionBinding
    Binding binding = new Binding(){
      Object getVariable(String name) {
        def returnValue =
          ClaCommandOptionBinding.this.getBindingOptionValue(name)
        if( returnValue ){
          return returnValue
        }
        else {
          super.getVariable(name)
        }
      }
    }
    binding.setVariable("bind", this)

    GroovyShell shell = new GroovyShell(binding)

    def evalResults
    try {
      evalResults = shell.evaluate(options)
    }
    catch( all ){
      println "error in evaluation: $all"
      all.printStackTrace()
      throw all
    }

    return flattenEvalResult(evalResults)
  }

  /**
   * This method could get very fancy if it wanted, but it's very simple at
   * the moment, it just adds the elements of any contained list to the
   * original list in place of itself.
   */
  private List<String> flattenEvalResult(evalResults) {
//    log.debug("options eval: $evalResults")
    if (!evalResults instanceof List) {
      // or, we could just call toString on whatever it is
      throw new OptionParsingException(
        "options must evaluate to a List, but got: $evalResults")
    }

    List<String> optionsResult = []
    evalResults.collect { evalResult ->
      if (evalResult == null) {
        return
      }
      if (evalResult instanceof Iterable) {
        // using Iterable even turns strings into list so of chars,
        // that's no good
        ((Iterable) evalResult).each {
          optionsResult << it.toString()
        }
      }
      else {
        optionsResult << evalResult.toString()
      }
    }
    return optionsResult
  }

  /**
   * When resolving names used in options scripts, this will be called first
   * and will resolve to any property method on this class that is annotated
   * with {@link OptionBinding}.
   *
   * @return null if coudln't find any appriately named property that is marked,
   * or if the porperty getter itself returns null.
   */
  Object getBindingOptionValue(String varName){
    Method m = null
    ClaUtil.getMethodsForPropertiesWithAnnotation(
      ClaCommandOptionBinding, OptionBinding).each
      { propName, method ->
//        log.debug "$varName - $propName"
        if( propName == varName ){
          m = method
        }
      }

    if( m != null ){
      return m.invoke(this, [] as Class[])
    }
    else {
      return null
    }
  }


  Map<String, String> getOptionsDocs(){
    def result = [:]
    ClaUtil.getMethodsForPropertiesWithAnnotation(
      ClaCommandOptionBinding, OptionBinding).
    each { name, method ->
      result[name] = method.getAnnotation(OptionBinding).value()
    }

    return result
  }

  @OptionBinding("any of the static methods on net.intellij.plugins.sbt.cla.util.ClaUtil")
  Class<ClaUtil> getUtil(){
    ClaUtil
  }

  @OptionBinding("com.intellij.openapi.vcs.changes.ChangeList")
  ChangeList getChangeList(){
    changeList
  }

  @OptionBinding("get the list of changes as a single space separated string.  Absolute paths, deleted files not included.")
  String getChangeListString(){
    changeList.changes*.virtualFile.join(" ")
  }

  @OptionBinding("com.intellij.openapi.roots.ProjectRootManager")
  ProjectRootManager getRootManager(){
    ProjectRootManager.getInstance(projectComponent.project)
  }
  
  @OptionBinding("com.intellij.openapi.roots.ProjectFileIndex")
  ProjectFileIndex getFileIndex(){
    return rootManager.fileIndex
  }

  @OptionBinding("com.intellij.openapi.roots.ProjectFileIndex")
  VirtualFile[] getContentRoots(){
    return rootManager.contentRoots
  }

  /**
   * this will return the path of all changes relative to the contentRoot
   * indicated.
   * The if any change in the current changeList does not actually live
   * under the contentRoot indicated, the path returned will be the absolute
   * path.  If you need to deal with changelists consisting of changes that
   * cross contentRoots, you'll have to filter changes by contentRoot or
   * something like that.
   */
  @OptionBinding("return a List<String> of file paths of each change relative to the contentRoot indicated")
  List<String> getRelativePaths(int contentRootIndex) {
    VirtualFile root = contentRoots[contentRootIndex]

    return changeList.changes.collect { Change change ->
      // the "-'/'" chops off any beginning slash because the paths
      // we want to return are relative
      return getContentRevision(change).file.path - root.getPath() - '/'
    }
  }

  /**
   * Tries to get a content revision from a file, prefers the "afterRevision"
   * but if that's not available (because file was deleted)
   * will return the "beforeRevsion".
   *
   * @param change may not be null
   * @return may return null if both content revisions on the
   * given change are null
   */
  ContentRevision getContentRevision(Change change){
    if (change.type == DELETED) {
      return change.beforeRevision
    }
    else {
      return change.afterRevision
    }

  }

  @OptionBinding("returns the absolute filename of a file containing the list of paths (relative to the indicated contentRoot) of files in the changelist")
  String getRelativePathsFile(int contentRootIndex){
    List<String> relativeChanges = getRelativePaths(contentRootIndex)
    return ClaUtil.writeLnToTempFile(relativeChanges).path
  }


}

