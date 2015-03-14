package org.pitest.mutationtest.commandline;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.predicate.Predicate;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.plugin.ClientClasspathPlugin;
import org.pitest.util.PitError;

public class PluginFilter implements Predicate<String>{
  
  private final Set<String> includedClassPathElement = new HashSet<String>();
  
  public PluginFilter(PluginServices plugin) {
    FCollection.mapTo(plugin.findClientClasspathPlugins(), classToLocation(), includedClassPathElement);  
  }

  private static F<ClientClasspathPlugin, String> classToLocation() {
    return new F<ClientClasspathPlugin, String>() {
      public String apply(ClientClasspathPlugin a) {
        try {
          return new File(a.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getCanonicalPath();
        } catch (final IOException ex) {
          throw createPitErrorForExceptionOnClass(ex, a);
        } catch (URISyntaxException ex) {
          throw createPitErrorForExceptionOnClass(ex, a);
        }
      }

      private PitError createPitErrorForExceptionOnClass(Exception ex, ClientClasspathPlugin clazz) {
        return new PitError("Error getting location of class " + clazz, ex);
      }
    };
  }

  public Boolean apply(String a) {
    return includedClassPathElement.contains(a);
  }

}
