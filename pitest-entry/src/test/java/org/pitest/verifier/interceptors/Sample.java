package org.pitest.verifier.interceptors;

import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.classinfo.ClassName;

public class Sample {
    ClassName className;
    ClassTree clazz;

    public Sample(ClassName name, ClassTree tree) {
        this.className = name;
        this.clazz = tree;
    }

}
