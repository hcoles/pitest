package org.pitest.bytecode.analysis;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;
import org.pitest.classinfo.ClassName;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.MutableList;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MethodName;

public class MethodTree {

  private final ClassName owner;
  private final MethodNode rawNode;
  private FunctionalList<AbstractInsnNode> lazyInstructions;

  public MethodTree(ClassName owner, MethodNode rawNode) {
    this.owner = owner;
    this.rawNode = rawNode;
  }

  public MethodNode rawNode() {
    return this.rawNode;
  }

  public Location asLocation() {
    return Location.location(this.owner,MethodName.fromString(this.rawNode.name), this.rawNode.desc);
  }

  public FunctionalList<AbstractInsnNode> instructions() {
    if (this.lazyInstructions != null) {
      return this.lazyInstructions;
    }

    return createInstructionList();
  }

  public boolean isSynthetic() {
    return (this.rawNode.access & Opcodes.ACC_SYNTHETIC) != 0;
  }

  public FunctionalList<AnnotationNode> annotations() {
    final FunctionalList<AnnotationNode> annotaions = new MutableList<>();
    if (this.rawNode.invisibleAnnotations != null) {
      annotaions.addAll(this.rawNode.invisibleAnnotations);
    }
    if (this.rawNode.visibleAnnotations != null) {
      annotaions.addAll(this.rawNode.visibleAnnotations);
    }
    return annotaions;
  }

  private FunctionalList<AbstractInsnNode> createInstructionList() {
    final List<AbstractInsnNode> list = new LinkedList<>();
    final ListIterator<AbstractInsnNode> it = this.rawNode.instructions.iterator();
    while (it.hasNext()) {
        list.add(it.next());
    }
    this.lazyInstructions = new MutableList<>(list);
    return this.lazyInstructions;
  }

}
