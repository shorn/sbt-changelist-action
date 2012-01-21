package net.intellij.plugins.sbt.cla.action

import org.junit.Test
import junit.framework.AssertionFailedError

class ClaCommandOptionBindingTest {

  @Test
  void testListProperties(){
    ClaCommandOptionBinding binding = new ClaCommandOptionBinding(null)

    assertContains(binding.optionsDocs, "helloWorld", "say hello to the world")
  }

  static void assertContains(Map map, Object key, Object value){
    if( !map.containsKey(key) ){
      throw new AssertionFailedError("map does not contain key [$key]: $map")
    }

    def actual = map.get(key)
    if( actual != value ){
      throw new AssertionFailedError("expected key [$key] to map to [$value], but was: $actual")
    }
  }
}
