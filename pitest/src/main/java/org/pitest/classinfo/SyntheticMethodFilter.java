package org.pitest.classinfo;

import org.objectweb.asm.Opcodes;
import org.pitest.functional.F5;

/**
 * Filters out synthetic and bridge methods, but allows synthetic
 * lambdas. This logic is duplicated in org.pitest.bytecode.analysis.MethodTree
 * when identifying code lines.
 */
public enum SyntheticMethodFilter implements
        F5<Integer, String, String, String, String[], Boolean> {

    INSTANCE;

    @Override
    public Boolean apply(final Integer access, final String name,
                         final String desc, final String signature, final String[] exceptions) {
        return (!isSynthetic(access, name) && !isBridge(access));
    }

    private static boolean isSynthetic(final int access, String name) {
        return (access & Opcodes.ACC_SYNTHETIC) != 0 && !name.startsWith("lambda$");
    }

    private static boolean isBridge(final Integer access) {
        return (access & Opcodes.ACC_BRIDGE) != 0;
    }

}
