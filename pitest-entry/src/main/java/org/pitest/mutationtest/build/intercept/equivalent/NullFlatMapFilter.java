package org.pitest.mutationtest.build.intercept.equivalent;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.mutators.NullReturnValsMutator;
import org.pitest.sequence.Context;
import org.pitest.sequence.QueryParams;
import org.pitest.sequence.QueryStart;
import org.pitest.sequence.SequenceMatcher;
import org.pitest.sequence.Slot;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.pitest.bytecode.analysis.InstructionMatchers.anyInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.isInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.methodCallTo;
import static org.pitest.bytecode.analysis.InstructionMatchers.notAnInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.opCode;

public class NullFlatMapFilter implements MutationInterceptor {

    private static final boolean DEBUG = false;
    private static final Slot<AbstractInsnNode> MUTATED_INSTRUCTION = Slot.create(AbstractInsnNode.class);

    static final SequenceMatcher<AbstractInsnNode> RETURN_EMPTY_STREAM = QueryStart
            .any(AbstractInsnNode.class)
            .then(methodCallTo(ClassName.fromClass(Stream.class), "empty"))
            .then(opCode(Opcodes.ARETURN).and(isInstruction(MUTATED_INSTRUCTION.read())))
            .zeroOrMore(QueryStart.match(anyInstruction()))
            .compile(QueryParams.params(AbstractInsnNode.class)
                    .withIgnores(notAnInstruction())
                    .withDebug(DEBUG)
            );


    private ClassTree currentClass;

    @Override
    public InterceptorType type() {
        return InterceptorType.FILTER;
    }

    @Override
    public void begin(ClassTree clazz) {
        currentClass = clazz;
    }

    @Override
    public Collection<MutationDetails> intercept(Collection<MutationDetails> mutations, Mutater unused) {
        return mutations.stream()
                .filter(m -> !this.mutatesStreamEmpty(m))
                .collect(Collectors.toList());
    }

    private boolean mutatesStreamEmpty(MutationDetails mutationDetails) {
        if (!mutationDetails.getMutator().equals(NullReturnValsMutator.NULL_RETURN_VALUES.getGloballyUniqueId())) {
            return false;
        }

        MethodTree method = currentClass.method(mutationDetails.getId().getLocation()).get();
        if (!method.isPrivate() || !method.returns(ClassName.fromClass(Stream.class))) {
            return false;
        }

        final Context<AbstractInsnNode> context = Context.start(method.instructions(), DEBUG);
        context.store(MUTATED_INSTRUCTION.write(), method.instruction(mutationDetails.getInstructionIndex()));
        return RETURN_EMPTY_STREAM.matches(method.instructions(), context);
    }

    @Override
    public void end() {
        currentClass = null;
    }
}
