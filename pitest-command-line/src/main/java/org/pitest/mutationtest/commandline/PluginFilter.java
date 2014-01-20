package org.pitest.mutationtest.commandline;

import java.util.HashSet;
import java.util.Set;

import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.predicate.Predicate;
import org.pitest.mutationtest.config.PluginServices;

public class PluginFilter implements Predicate<String>{
  
  private final Set<String> includedClassPathElement = new HashSet<String>();
  
  public PluginFilter(PluginServices plugin) {
    FCollection.mapTo(plugin.findClientClasspathPlugins(), classToLocation(), includedClassPathElement);  
  }

  private static F<Object, String> classToLocation() {
    return new F<Object, String>() {
      public String apply(Object a) {
        return a.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
      }  
    };
  }

  public Boolean apply(String a) {
    return includedClassPathElement.contains(a);
  }

}
