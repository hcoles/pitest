package org.pitest.mutationtest.build.intercept;

import org.objectweb.asm.tree.AbstractInsnNode;

public class Region {
    public final AbstractInsnNode start;
    public final AbstractInsnNode end;
    public Region(AbstractInsnNode start, AbstractInsnNode end) {
        this.start = start;
        this.end = end;
    }

}