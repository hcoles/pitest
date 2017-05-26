package org.pitest.mutationtest.filter.support;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.pitest.mutationtest.LocationMother.aLocation;
import static org.pitest.mutationtest.LocationMother.aMutationId;
import static org.pitest.mutationtest.engine.MutationDetailsMother.aMutationDetail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.CodeSource;
import org.pitest.coverage.analysis.Block;
import org.pitest.functional.F;
import org.pitest.functional.Option;
import org.pitest.mutationtest.ReportTestBase;
import org.pitest.mutationtest.engine.MutationDetails;

@RunWith(MockitoJUnitRunner.class)
public class MutationOnSwitchInstructionLookupImplTest extends ReportTestBase {

  @Mock
  private CodeSource                 source;

  @Mock
  private ClassReader                classReader;

  @Mock
  private ControlFlowAnalyserWrapper controlFlowAnalyserWrapper;

  private Block createBlock(final int firstInstruction,
      final int lastInstruction, final int lineNo) {
    final HashSet<Integer> lines = new HashSet<Integer>();
    lines.add(lineNo);
    return new Block(firstInstruction, lastInstruction, lines);
  }

  private LookupSwitchInsnNode createLookupSwitchInsnNode() {
    return new LookupSwitchInsnNode(new LabelNode(), new int[] {},
        new LabelNode[] {});
  }

  private ClassNode prepareClassNode(final ClassName className,
      final String method, final int mutationLineNumber,
      final AbstractInsnNode instructionFromMutatedBlock) {

    given(source.fetchClassBytes(className))
        .willReturn(Option.some(new byte[] {}));

    final ClassNode classNode = new ClassNode();
    classNode.methods = new ArrayList();

    final MethodNode methodNode = new MethodNode();
    methodNode.name = method;
    classNode.methods.add(methodNode);

    methodNode.instructions = new InsnList();
    methodNode.instructions.add(new LabelNode());
    methodNode.instructions.add(new LabelNode());
    methodNode.instructions.add(new LabelNode());
    methodNode.instructions.add(new LabelNode());
    methodNode.instructions.add(new LabelNode());
    methodNode.instructions.add(instructionFromMutatedBlock);
    methodNode.instructions.add(new LabelNode());
    methodNode.instructions.add(new LabelNode());
    methodNode.instructions.add(new LabelNode());

    final List<Block> blocks = asList(createBlock(0, 1, 5),
        createBlock(2, 3, 5), createBlock(4, 5, mutationLineNumber),
        createBlock(6, 7, 14));
    given(controlFlowAnalyserWrapper.analyze(methodNode)).willReturn(blocks);

    return classNode;
  }

  private MutationOnSwitchInstructionLookupImpl prepareTestee(
      final ClassNode classNode) {
    return new MutationOnSwitchInstructionLookupImpl(new Supplier<ClassNode>() {
      @Override
      public ClassNode get() {
        return classNode;
      }
    }, new F<byte[], ClassReader>() {
      @Override
      public ClassReader apply(final byte[] bytes) {
        return classReader;
      }
    }, controlFlowAnalyserWrapper) {
      ;
    };
  }

  @Test
  public void shouldReturnFalseWhenCodeSourceReturnsNoneClassBytes()
      throws Exception {
    // given
    final ClassName className = new ClassName("Foo");
    final String method = "bar";
    final int lineNumber = 12;

    final MutationDetails mutation = aMutationDetail()
        .withId(aMutationId()
            .withLocation(aLocation().withClass(className).withMethod(method)))
        .withLineNumber(lineNumber).build();

    given(source.fetchClassBytes(className)).willReturn(Option.<byte[]> none());

    final MutationOnSwitchInstructionLookup testee = prepareTestee(
        new ClassNode());

    // when
    final boolean mutationOnSwitchInstruction = testee
        .isMutationOnSwitchInstruction(mutation, source);

    // then
    assertThat(mutationOnSwitchInstruction).isFalse();
  }

  @Test
  public void shouldReturnFalseWhenMutationMethodNotFoundInClass()
      throws Exception {
    // given
    final ClassName className = new ClassName("Foo");
    final String method = "bar";
    final int mutationLineNumber = 12;

    final MutationDetails mutation = aMutationDetail()
        .withId(aMutationId()
            .withLocation(aLocation().withClass(className).withMethod(method)))
        .withLineNumber(mutationLineNumber).build();

    final String otherMethod = "baz";
    given(source.fetchClassBytes(className))
        .willReturn(Option.some(new byte[] {}));

    final ClassNode classNode = new ClassNode();
    classNode.methods = new ArrayList();

    final MethodNode methodNode = new MethodNode();
    methodNode.name = otherMethod;

    classNode.methods.add(methodNode);

    final MutationOnSwitchInstructionLookup testee = prepareTestee(classNode);

    // when
    final boolean mutationOnSwitchInstruction = testee
        .isMutationOnSwitchInstruction(mutation, source);

    // then
    assertThat(mutationOnSwitchInstruction).isFalse();
    verify(classReader).accept(classNode, ClassReader.EXPAND_FRAMES);
    verifyZeroInteractions(controlFlowAnalyserWrapper);
  }

  @Test
  public void shouldReturnFalseWhenMutationOnDifferentLineThenSwitchInstructionBlock()
      throws Exception {
    // given
    final ClassName className = new ClassName("Foo");
    final String method = "bar";
    final int mutationLineNumber = 12;

    final MutationDetails mutation = aMutationDetail()
        .withId(aMutationId()
            .withLocation(aLocation().withClass(className).withMethod(method)))
        .withLineNumber(mutationLineNumber).build();

    final TableSwitchInsnNode instructionFromMutatedBlock = new TableSwitchInsnNode(
        1, 1, new LabelNode());

    final int mutationLineNumber1 = 66;
    final ClassNode classNode = prepareClassNode(className, method,
        mutationLineNumber1, instructionFromMutatedBlock);

    final MutationOnSwitchInstructionLookup testee = prepareTestee(classNode);

    // when
    final boolean mutationOnSwitchInstruction = testee
        .isMutationOnSwitchInstruction(mutation, source);

    // then
    assertThat(mutationOnSwitchInstruction).isFalse();
    verify(classReader).accept(classNode, ClassReader.EXPAND_FRAMES);
  }

  @Test
  public void shouldReturnFalseWhenMutationOnMethodWithoutSwitchInstruction()
      throws Exception {
    // given
    final ClassName className = new ClassName("Foo");
    final String method = "bar";
    final int mutationLineNumber = 12;

    final MutationDetails mutation = aMutationDetail()
        .withId(aMutationId()
            .withLocation(aLocation().withClass(className).withMethod(method)))
        .withLineNumber(mutationLineNumber).build();
    final LabelNode instructionFromMutatedBlock = new LabelNode();

    final ClassNode classNode = prepareClassNode(className, method,
        mutationLineNumber, instructionFromMutatedBlock);

    final MutationOnSwitchInstructionLookup testee = prepareTestee(classNode);

    // when
    final boolean mutationOnSwitchInstruction = testee
        .isMutationOnSwitchInstruction(mutation, source);

    // then
    assertThat(mutationOnSwitchInstruction).isFalse();
    verify(classReader).accept(classNode, ClassReader.EXPAND_FRAMES);
  }

  @Test
  public void shouldReturnTrueWhenMutationOnMethodWithLookupSwitchInstruction()
      throws Exception {
    // given
    final ClassName className = new ClassName("Foo");
    final String method = "bar";
    final int mutationLineNumber = 12;

    final MutationDetails mutation = aMutationDetail()
        .withId(aMutationId()
            .withLocation(aLocation().withClass(className).withMethod(method)))
        .withLineNumber(mutationLineNumber).build();

    final LookupSwitchInsnNode instructionFromMutatedBlock = createLookupSwitchInsnNode();

    final ClassNode classNode = prepareClassNode(className, method,
        mutationLineNumber, instructionFromMutatedBlock);

    final MutationOnSwitchInstructionLookup testee = prepareTestee(classNode);

    // when
    final boolean mutationOnSwitchInstruction = testee
        .isMutationOnSwitchInstruction(mutation, source);

    // then
    assertThat(mutationOnSwitchInstruction).isTrue();
    verify(classReader).accept(classNode, ClassReader.EXPAND_FRAMES);
  }

  @Test
  public void shouldReturnTrueWhenMutationOnMethodWithTableSwitchInstruction()
      throws Exception {
    // given
    final ClassName className = new ClassName("Foo");
    final String method = "bar";
    final int mutationLineNumber = 12;

    final MutationDetails mutation = aMutationDetail()
        .withId(aMutationId()
            .withLocation(aLocation().withClass(className).withMethod(method)))
        .withLineNumber(mutationLineNumber).build();

    final TableSwitchInsnNode instructionFromMutatedBlock = new TableSwitchInsnNode(
        1, 1, new LabelNode());

    final ClassNode classNode = prepareClassNode(className, method,
        mutationLineNumber, instructionFromMutatedBlock);

    final MutationOnSwitchInstructionLookup testee = prepareTestee(classNode);

    // when
    final boolean mutationOnSwitchInstruction = testee
        .isMutationOnSwitchInstruction(mutation, source);

    // then
    assertThat(mutationOnSwitchInstruction).isTrue();
    verify(classReader).accept(classNode, ClassReader.EXPAND_FRAMES);
  }
}
