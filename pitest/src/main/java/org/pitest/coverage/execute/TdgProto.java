package org.pitest.coverage.execute;

import java.util.Collection;

public interface TdgProto {
    public void recordTestUnitsName(String className, Collection<String> methodNames);
}
