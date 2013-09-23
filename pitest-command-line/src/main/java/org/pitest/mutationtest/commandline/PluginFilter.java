package org.pitest.mutationtest.commandline;

import java.util.HashSet;
import java.util.Set;

import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.predicate.Predicate;
import org.pitest.mutationtest.PluginServices;
import org.pitest.plugin.ToolClasspathPlugin;

public class PluginFilter implements Predicate<String>{
  
  private final Set<String> excludedClassPathElement = new HashSet<String>();
  
  public PluginFilter() {
    final Iterable<? extends ToolClasspathPlugin> nonRuntimePlugins = PluginServices.findToolClasspathPlugins();
    FCollection.mapTo(nonRuntimePlugins, classToLocation(), excludedClassPathElement);   
  }

  private static F<ToolClasspathPlugin, String> classToLocation() {
    return new F<ToolClasspathPlugin, String>() {
      public String apply(ToolClasspathPlugin a) {
        return a.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
      }  
    };
  }

  public Boolean apply(String a) {
    return !excludedClassPathElement.contains(a);
  }

}
