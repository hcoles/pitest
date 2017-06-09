package org.pitest.mutationtest.build;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.pitest.classinfo.ClassName;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.FunctionalList;

public class ClassTree {
  
  private final ClassNode rawNode;
  private FunctionalList<MethodTree> lazyMethods; 

  public ClassTree(ClassNode rawNode) {
    this.rawNode = rawNode;
  }
  
  public static ClassTree fromBytes(byte[] bytes) {
    final ClassReader cr = new ClassReader(bytes);
    final ClassNode classNode = new ClassNode();
    cr.accept(classNode, ClassReader.EXPAND_FRAMES);
    return new ClassTree(classNode);
  }
  

  public FunctionalList<MethodTree> methods() {
    if (lazyMethods != null) {
      return lazyMethods;
    }
    lazyMethods = FCollection.map(rawNode.methods, toTree(name()));
    return lazyMethods;
  }
  
  private static F<MethodNode, MethodTree> toTree(final ClassName name) {
    return new F<MethodNode, MethodTree>() {
      @Override
      public MethodTree apply(MethodNode a) {
        return new MethodTree(name,a);
      }
      
    };
  }

  public ClassName name() {
    return ClassName.fromString(rawNode.name);
  }
  
  public ClassNode rawNode() {
    return rawNode;
  }

}
