package org.pitest.mutationtest.build.intercept.equivalent;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.mutators.NullReturnValsMutator;
import org.pitest.sequence.Context;
import org.pitest.sequence.Match;
import org.pitest.sequence.QueryParams;
import org.pitest.sequence.QueryStart;
import org.pitest.sequence.SequenceMatcher;
import org.pitest.sequence.Slot;
import org.pitest.sequence.SlotRead;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.pitest.bytecode.analysis.InstructionMatchers.anyInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.isA;
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

    private static final Slot<ClassName> METHOD_OWNER = Slot.create(ClassName.class);
    private static final Slot<String> METHOD_DESC = Slot.create(String.class);
    static final SequenceMatcher<AbstractInsnNode> HAS_FLAT_MAP_CALL = QueryStart
            .any(AbstractInsnNode.class)
            .then(dynamicCallTo(METHOD_OWNER.read(), METHOD_DESC.read()))
            .then(methodCallTo(ClassName.fromClass(Stream.class), "flatMap"))
            .zeroOrMore(QueryStart.match(anyInstruction()))
            .compile(QueryParams.params(AbstractInsnNode.class)
                    .withIgnores(notAnInstruction().or(isA(LabelNode.class)))
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
        return RETURN_EMPTY_STREAM.matches(method.instructions(), context)
                && calledOnlyFromFlatMap(mutationDetails.getId().getLocation());
    }

    private boolean calledOnlyFromFlatMap(Location location) {
        MethodTree mutated = currentClass.method(location).get();

        boolean flatMapCallFound = false;
        for (MethodTree each : currentClass.methods()) {
            if (callsTarget(mutated, each)) {
                flatMapCallFound = isFlatMapCall(mutated, each);
                if (!flatMapCallFound) {
                    return false;
                }
            }
        }

        return flatMapCallFound;

    }

    private boolean callsTarget(MethodTree target, MethodTree method) {
        return method.instructions().stream()
                .anyMatch(callTo(target.asLocation().getClassName(), target.asLocation().getMethodName().name())
                        .or(dynamicCallTo(target.asLocation().getClassName(), target.asLocation().getMethodName().name())));
    }

    private Predicate<AbstractInsnNode> callTo(ClassName className, String name) {
        return n -> {
            if (n instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) n;
                return call.owner.equals(className.asInternalName()) && call.name.equals(name);
            }
            return false;
        };
    }

    private boolean isFlatMapCall(MethodTree mutated, MethodTree each) {
        final Context<AbstractInsnNode> context = Context.start(each.instructions(), DEBUG);
        context.store(METHOD_OWNER.write(), mutated.asLocation().getClassName());
        context.store(METHOD_DESC.write(), mutated.asLocation().getMethodName().name());
        boolean hasFlatMapCall = HAS_FLAT_MAP_CALL.matches(each.instructions(), context);
        return hasFlatMapCall;
    }


    private static Match<AbstractInsnNode> dynamicCallTo(SlotRead<ClassName> owner, SlotRead<String> desc) {
        return (c, t) -> dynamicCallTo(c.retrieve(owner).get(), c.retrieve(desc).get()).test(t);
    }

    private static Predicate<AbstractInsnNode> dynamicCallTo(ClassName owner, String desc) {
        return (t) -> {
            if ( t instanceof InvokeDynamicInsnNode) {
                InvokeDynamicInsnNode call = (InvokeDynamicInsnNode) t;
                return Arrays.stream(call.bsmArgs)
                        .anyMatch(isHandle(owner, desc));
            }
            return false;
        };
    }

    private static Predicate<Object> isHandle(ClassName owner, String name) {
        return o -> {
            if (o instanceof Handle) {
                Handle handle = (Handle) o;
                return handle.getOwner().equals(owner.asInternalName()) &&
                        handle.getName().equals(name);
            }
            return false;
        };
    }

    @Override
    public void end() {
        currentClass = null;
    }
}
