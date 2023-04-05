package org.pitest.mutationtest.environment;

import org.pitest.plugin.ClientClasspathPlugin;

import java.lang.instrument.ClassFileTransformer;

public interface TransformationPlugin extends ClientClasspathPlugin {

    ClassFileTransformer makeTransformer();

}
