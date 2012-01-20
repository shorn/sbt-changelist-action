package net.intellij.plugins.sbt.cla.action

import com.intellij.openapi.diagnostic.Logger
import groovy.transform.InheritConstructors

import java.lang.annotation.Retention
import java.lang.annotation.Target
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.ElementType
import java.lang.reflect.Method
import java.lang.annotation.Annotation

class ClaCommandOptionBinding {
  private final Logger log = Logger.getInstance(getClass())

  /**
   * This method doesn't do any exception handling, especially
   * {@link GroovyShell#evaluate} exceptions are propogated.
   *
   * @throws OptionParsingException if the expression doesn't evaluate
   * to a list
   */
  List parseOptions(String options){
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

    def result = shell.evaluate(options)

    log.debug("options eval: $result")
    if( result instanceof  List ){
      return (List) result
    }
    else {
      throw new OptionParsingException(
        "options must evaluate to a String<List>, but got: $result")
    }
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


}

/**
 * value is expected to be some documentation of the option
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface OptionBinding{
  String value()
}

@InheritConstructors
class OptionParsingException extends RuntimeException{ }
