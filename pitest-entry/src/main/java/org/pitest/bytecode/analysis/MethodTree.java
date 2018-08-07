package org.pitest.bytecode.analysis;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MethodName;

public class MethodTree {

  private final ClassName owner;
  private final MethodNode rawNode;
  private List<AbstractInsnNode> lazyInstructions;

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
    
  /**
   * Looks backwards for the next real instruction node (i.e. not a label or line number)
   * @param index index to work backwards from
   * @return The previous instruction
   */
  public AbstractInsnNode realInstructionBefore(int index) {
    AbstractInsnNode candidate = instructions().get(index - 1);
    if (candidate.getOpcode() == -1) {
      return realInstructionBefore(index - 1);
    }
    return candidate;
  }
  
  public AbstractInsnNode instruction(int index) {
      return instructions().get(index);
  }
  
  public List<AbstractInsnNode> instructions() {
    if (this.lazyInstructions != null) {
      return this.lazyInstructions;
    }

    return createInstructionList();
  }

  public boolean isSynthetic() {
    return (this.rawNode.access & Opcodes.ACC_SYNTHETIC) != 0;
  }

  public List<AnnotationNode> annotations() {
    final List<AnnotationNode> annotaions = new ArrayList<>();
    if (this.rawNode.invisibleAnnotations != null) {
      annotaions.addAll(this.rawNode.invisibleAnnotations);
    }
    if (this.rawNode.visibleAnnotations != null) {
      annotaions.addAll(this.rawNode.visibleAnnotations);
    }
    return annotaions;
  }

  private List<AbstractInsnNode> createInstructionList() {
    final List<AbstractInsnNode> list = new LinkedList<>();
    final ListIterator<AbstractInsnNode> it = this.rawNode.instructions.iterator();
    while (it.hasNext()) {
        list.add(it.next());
    }
    this.lazyInstructions = new ArrayList<>(list);
    return this.lazyInstructions;
  }

}
