package org.pitest.mutationtest.filter.support;

import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.CodeSource;
import org.pitest.coverage.ClassLine;
import org.pitest.coverage.analysis.Block;
import org.pitest.functional.F;
import org.pitest.functional.Option;
import org.pitest.mutationtest.engine.MethodName;
import org.pitest.mutationtest.engine.MutationDetails;

public class MutationOnSwitchInstructionLookupImpl
    implements MutationOnSwitchInstructionLookup {

  private static final Supplier<ClassNode>            SIMPLE_CLASS_NODE_SUPPLIER           = new Supplier<ClassNode>() {
                                                                                             @Override
                                                                                             public ClassNode get() {
                                                                                               return new ClassNode();
                                                                                             }
                                                                                           };

  private static final F<byte[], ClassReader>         SIMPLE_CLASS_READER_FUNCTION         = new F<byte[], ClassReader>() {
                                                                                             @Override
                                                                                             public ClassReader apply(
                                                                                                 final byte[] bytes) {
                                                                                               return new ClassReader(
                                                                                                   bytes);
                                                                                             }
                                                                                           };

  private static final ControlFlowAnalyserWrapperImpl SIMPLE_CONTROL_FLOW_ANALYSER_WRAPPER = new ControlFlowAnalyserWrapperImpl();

  private final Supplier<ClassNode>                   classNodeSupplier;

  private final F<byte[], ClassReader>                classReaderFunction;

  private final ControlFlowAnalyserWrapper            controlFlowAnalyserWrapper;

  public MutationOnSwitchInstructionLookupImpl() {
    this(SIMPLE_CLASS_NODE_SUPPLIER, SIMPLE_CLASS_READER_FUNCTION,
        SIMPLE_CONTROL_FLOW_ANALYSER_WRAPPER);
  }

  public MutationOnSwitchInstructionLookupImpl(
      final Supplier<ClassNode> classNodeSupplier,
      final F<byte[], ClassReader> classReaderFunction,
      final ControlFlowAnalyserWrapper controlFlowAnalyserWrapper) {
    this.classNodeSupplier = classNodeSupplier;
    this.classReaderFunction = classReaderFunction;
    this.controlFlowAnalyserWrapper = controlFlowAnalyserWrapper;
  }

  private boolean isBlockForMutatedLine(final Block block,
      final int lineNumber) {
    return block.getLines().contains(lineNumber);
  }

  @Override
  public boolean isMutationOnSwitchInstruction(final MutationDetails mutation,
      final CodeSource source) {
    final ClassName className = mutation.getClassName();

    final Option<byte[]> maybeBytes = source.fetchClassBytes(className);

    if (maybeBytes.hasSome()) {
      final byte[] bytes = maybeBytes.value();
      final ClassReader cr = classReaderFunction.apply(bytes);
      final ClassNode classNode = classNodeSupplier.get();

      cr.accept(classNode, ClassReader.EXPAND_FRAMES);

      for (final Object m : classNode.methods) {
        final MethodNode methodNode = (MethodNode) m;
        final MethodName methodName = MethodName.fromString(methodNode.name);
        if (methodName.equals(mutation.getMethod())) {

          final List<Block> blocks = controlFlowAnalyserWrapper
              .analyze(methodNode);
          for (final Block block : blocks) {
            final ClassLine classLine = mutation.getClassLine();
            final int lineNumber = classLine.getLineNumber();
            if (isBlockForMutatedLine(block, lineNumber)) {
              final int firstInstruction = block.getFirstInstruction();
              final int lastInstruction = block.getLastInstruction();

              for (int i = firstInstruction; i <= lastInstruction; i++) {
                final AbstractInsnNode abstractInsnNode = methodNode.instructions
                    .get(i);
                final boolean isSwitchInstruction = isSwitchInstruction(
                    abstractInsnNode);
                if (isSwitchInstruction) {
                  return true;
                }
              }

            }
          }
        }

      }
    }
    return false;
  }

  private boolean isSwitchInstruction(final AbstractInsnNode abstractInsnNode) {
    return abstractInsnNode instanceof TableSwitchInsnNode
        || abstractInsnNode instanceof LookupSwitchInsnNode;
  }
}
