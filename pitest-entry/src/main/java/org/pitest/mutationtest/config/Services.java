package org.pitest.mutationtest.config;

import java.io.File;
import java.util.Collection;

public interface Services {
    <S> Collection<S> load(Class<S> ifc);
    Collection<File> findPluginDescriptors(Class<?> ifc);
}
