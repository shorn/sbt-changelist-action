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
   * This method doesn't do any exception handling, especially
   * {@link GroovyShell#evaluate} exceptions are propogated.
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

    GroovyShell shell = new GroovyShell(binding)

    def evalResults = shell.evaluate(options)

    return flattenEvalResult(evalResults)
  }

  /**
   * This method could get very fancy if it wanted
   */
  private List<String> flattenEvalResult(evalResults) {
    log.debug("options eval: $evalResults")
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
        log.debug "$varName - $propName"
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

  @OptionBinding("returns the name of a temp file containing a line for each changed file relative to contentRoot[0]")
  String getChangeListRelativeFile(){
    return ClaUtil.writeLnToTempFile(getChangesRelativeToFirstContentRoot()).path
  }

  @OptionBinding("returns List<String> of file paths relative to contentRoots[0]")
  List<String> getChangesRelativeToFirstContentRoot() {
    changeList.changes*.virtualFile.path*.minus(contentRoots[0].path + '/')
  }

  @OptionBinding("get the list of changes as a single space separated string")
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



}

