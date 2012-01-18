package net.intellij.plugins.sbt.changelistaction.action

import com.intellij.openapi.diagnostic.Logger
import groovy.transform.InheritConstructors

class ClaCommandOptionBinding {
  private final Logger log = Logger.getInstance(getClass())
  
  String bindingName = "idea"

  String getHelloWorld(){
    return "hello World"
  }

  /**
   * This method doesn't do any exception handling, especially
   * {@link GroovyShell#evaluate} exceptions are propogated.
   *
   * @throws OptionParsingException if the expression doesn't evaluate
   * to a list
   */
  List parseOptions(String options){
    GroovyShell shell = new GroovyShell()
    shell.setVariable(bindingName, this)

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
}

@InheritConstructors
class OptionParsingException extends RuntimeException{ }
