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
    return rawNode;
  }

  public Location asLocation() {
    return Location.location(owner,MethodName.fromString(rawNode.name), rawNode.desc);
  }
  
  public FunctionalList<AbstractInsnNode> instructions() {
    if (lazyInstructions != null) {
      return lazyInstructions;
    }
    
    return createInstructionList();
  }
  
  public boolean isSynthetic() {
    return (rawNode.access & Opcodes.ACC_SYNTHETIC) != 0;
  }
  
  public FunctionalList<AnnotationNode> annotations() {
    FunctionalList<AnnotationNode> annotaions = new MutableList<AnnotationNode>();
    if (rawNode.invisibleAnnotations != null) {
      annotaions.addAll(rawNode.invisibleAnnotations);
    }
    if (rawNode.visibleAnnotations != null) {
      annotaions.addAll(rawNode.visibleAnnotations);
    }
    return annotaions;
  }

  private FunctionalList<AbstractInsnNode> createInstructionList() {
    List<AbstractInsnNode> list = new LinkedList<AbstractInsnNode>();
    ListIterator<AbstractInsnNode> it = rawNode.instructions.iterator();
    while (it.hasNext()) {
        list.add(it.next());
    }
    lazyInstructions = new MutableList<AbstractInsnNode>(list);
    return lazyInstructions;
  }
  
}
