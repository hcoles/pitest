package org.pitest.bytecode.analysis;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.RecordComponentNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.Location;

import static org.pitest.functional.Streams.asStream;

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

  public byte[] toBytes() {
    ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    rawNode().accept(classWriter);
    return classWriter.toByteArray();
  }

  public List<MethodTree> methods() {
    if (this.lazyMethods != null) {
      return this.lazyMethods;
    }
    this.lazyMethods = asStream(this.rawNode.methods)
            .map(toTree(name()))
            .collect(Collectors.toList());
    return this.lazyMethods;
  }

  public Optional<MethodTree> method(Location loc) {
   return methods().stream().filter(MethodMatchers.forLocation(loc)).findFirst();
  }

  public List<AnnotationNode> annotations() {
    final List<AnnotationNode> annotations = new ArrayList<>();
    if (this.rawNode.invisibleAnnotations != null) {
      annotations.addAll(this.rawNode.invisibleAnnotations);
    }
    if (this.rawNode.visibleAnnotations != null) {
      annotations.addAll(this.rawNode.visibleAnnotations);
    }
    return annotations;
  }

  public List<RecordComponentNode> recordComponents() {
    if (rawNode.recordComponents == null) {
      return Collections.emptyList();
    }

    return rawNode.recordComponents;
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

  public Set<Integer> codeLineNumbers() {
    return realMethods()
            .flatMap(m -> m.instructions().stream()
                    .filter(n -> n instanceof LineNumberNode)
                    .map(n -> ((LineNumberNode) n).line))
            .collect(Collectors.toSet());
  }

  public int numberOfCodeLines() {
    return codeLineNumbers().size();
  }

  /**
   * Methods, excluding bridges and synthetics
   */
  public Stream<MethodTree> realMethods() {
      return methods().stream()
              .filter(m -> (!m.isBridge() && !m.isSynthetic()) || m.isGeneratedLambdaMethod());
  }
  public boolean isAbstract() {
    return (this.rawNode.access & Opcodes.ACC_ABSTRACT) != 0;
  }

  public boolean isInterface() {
    return (this.rawNode.access & Opcodes.ACC_INTERFACE) != 0;
  }

  public boolean isSynthetic() {
    return (this.rawNode.access & Opcodes.ACC_SYNTHETIC) != 0;
  }

  public ClassTree rename(ClassName name) {
    this.rawNode.name = name.asInternalName();
    return this;
  }

  @Override
  public String toString() {
    final StringWriter writer = new StringWriter();
    this.rawNode.accept(new TraceClassVisitor(null, new Textifier(), new PrintWriter(
        writer)));
    return writer.toString();

  }

}
