package org.pitest.mutationtest.config;

import java.util.Collection;

public interface Services {
    <S> Collection<S> load(Class<S> ifc);
}
