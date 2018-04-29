package org.pitest.bytecode.analysis;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;
import org.pitest.classinfo.ClassName;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.engine.Location;

public class ClassTree {

  private final ClassNode rawNode;
  private List<MethodTree> lazyMethods;

  public ClassTree(ClassNode rawNode) {
    this.rawNode = rawNode;
  }

  public static ClassTree fromBytes(byte[] bytes) {
    final ClassReader cr = new ClassReader(bytes);
    final ClassNode classNode = new ClassNode();
    cr.accept(classNode, ClassReader.EXPAND_FRAMES);
    return new ClassTree(classNode);
  }


  public List<MethodTree> methods() {
    if (this.lazyMethods != null) {
      return this.lazyMethods;
    }
    this.lazyMethods = FCollection.map(this.rawNode.methods, toTree(name()));
    return this.lazyMethods;
  }

  public Optional<MethodTree> method(Location loc) {
   return methods().stream().filter(MethodMatchers.forLocation(loc)).findFirst();
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

  private static Function<MethodNode, MethodTree> toTree(final ClassName name) {
    return a -> new MethodTree(name,a);
  }

  public ClassName name() {
    return ClassName.fromString(this.rawNode.name);
  }

  public ClassNode rawNode() {
    return this.rawNode;
  }


  @Override
  public String toString() {
    final StringWriter writer = new StringWriter();
    this.rawNode.accept(new TraceClassVisitor(null, new Textifier(), new PrintWriter(
        writer)));
    return writer.toString();

  }

}
