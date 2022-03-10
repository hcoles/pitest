package org.pitest.mutationtest.config;

import org.pitest.util.ServiceLoader;

import java.util.Collection;

public class ServicesFromClassLoader implements Services {
    private final ClassLoader loader;

    public ServicesFromClassLoader(ClassLoader loader) {
        this.loader = loader;
    }

    @Override
    public <S> Collection<S> load(Class<S> ifc) {
        return ServiceLoader.load(ifc, loader);
    }
}
