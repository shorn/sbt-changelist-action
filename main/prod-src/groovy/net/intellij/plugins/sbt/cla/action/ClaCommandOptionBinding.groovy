package net.intellij.plugins.sbt.cla.action

import com.intellij.openapi.diagnostic.Logger
import groovy.transform.InheritConstructors

import java.lang.annotation.Retention
import java.lang.annotation.Target
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.ElementType
import java.lang.reflect.Method
import java.lang.annotation.Annotation
import net.intellij.plugins.sbt.cla.ClaProjectComponent

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vcs.changes.ChangeList

import net.intellij.plugins.sbt.cla.util.ClaUtil

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
    binding.setVariable("changeList", changeList)

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
      throw new OptionParsingException(
        "options must evaluate to a String<List>, but got: $evalResults")
    }

    List<String> optionsResult = []
    evalResults.collect { evalResult ->
      if (evalResult == null) {
        return
      }
      if (evalResult instanceof Iterable) {
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

  Object getBindingOptionValue(String varName){
    Method m = null
    ClaCommandOptionBinding.getMethodsForPropertiesWithAnnotation(
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
  
  static Method getMethodForProperty(
    Class<?> clazz,
    String propName,
    Class<?> propType)
  {
    String getterName = MetaProperty.getGetterName(
      propName, propType)
    clazz.getMethod(getterName, [] as Class[])
  }

  public static Map<String, Method> getMethodsForPropertiesWithAnnotation(
    Class<?> clazz,
    Class<? extends Annotation> annoClass)
  {
    def result = [:]
    clazz.metaClass.properties.each { metaProperty ->
      Method m = getMethodForProperty(clazz, metaProperty.name, metaProperty.type)
      m.annotations.each { anno ->
        if( annoClass.isInstance(anno) ){
          result[metaProperty.name] = m
        }
      }
    }

    result
  }

  Map<String, String> getOptionsDocs(){
    def result = [:]
    getMethodsForPropertiesWithAnnotation(
      ClaCommandOptionBinding, OptionBinding).
    each { name, method ->
      result[name] = method.getAnnotation(OptionBinding).value()
    }

    return result
  }

  @OptionBinding("say hello to the world")
  String getHelloWorld(){
    "hello World"
  }

  @OptionBinding("wibble")
  String getFlibble(){
    return "wibble"
  }
  
  @OptionBinding("get the list of changes")
  String getChangeListString(){
    List<VirtualFile> files = ClaUtil.getChangelistFiles(changeList)
    return files.join(" ")
  }



}

