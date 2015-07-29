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

public class PluginFilter implements Predicate<String> {

  private final Set<String> includedClassPathElement = new HashSet<String>();

  public PluginFilter(final PluginServices plugin) {
    FCollection.mapTo(plugin.findClientClasspathPlugins(), classToLocation(),
        this.includedClassPathElement);
  }

  private static F<ClientClasspathPlugin, String> classToLocation() {
    return new F<ClientClasspathPlugin, String>() {
      @Override
      public String apply(final ClientClasspathPlugin a) {
        try {
          return new File(a.getClass().getProtectionDomain().getCodeSource()
              .getLocation().toURI()).getCanonicalPath();
        } catch (final IOException ex) {
          throw createPitErrorForExceptionOnClass(ex, a);
        } catch (final URISyntaxException ex) {
          throw createPitErrorForExceptionOnClass(ex, a);
        }
      }

      private PitError createPitErrorForExceptionOnClass(final Exception ex,
          final ClientClasspathPlugin clazz) {
        return new PitError("Error getting location of class " + clazz, ex);
      }
    };
  }

  @Override
  public Boolean apply(final String a) {
    return this.includedClassPathElement.contains(a);
  }

}
