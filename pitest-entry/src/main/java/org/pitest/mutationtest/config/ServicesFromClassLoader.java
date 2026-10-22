package org.pitest.mutationtest.config;

import org.pitest.util.PitError;
import org.pitest.util.ServiceLoader;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

public class ServicesFromClassLoader implements Services {
    private final ClassLoader loader;

    public ServicesFromClassLoader(ClassLoader loader) {
        this.loader = loader;
    }

    @Override
    public <S> Collection<S> load(Class<S> ifc) {
        return ServiceLoader.load(ifc, loader);
    }

    @Override
    public Collection<File> findPluginDescriptors(Class<?> ifc) {
        try {
            final Collection<File> pluginDescriptors = new ArrayList<>();
            Enumeration<URL> e = this.loader.getResources("META-INF/services/" + ifc.getName());
            while (e.hasMoreElements()) {
                URL url = e.nextElement();
                if ("file".equals(url.getProtocol())) {
                    pluginDescriptors.add(Paths.get(url.toURI()).getParent().getParent().getParent().toFile());
                }
            }
            return pluginDescriptors;
        } catch (final IOException | URISyntaxException ex) {
            throw new PitError("Error finding plugin descriptor for " + ifc.getName(), ex);
        }
    }
}
