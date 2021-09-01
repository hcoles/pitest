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
import org.pitest.mutationtest.engine.gregor.mutators.returns.NullReturnValsMutator;
import org.pitest.sequence.Context;
import org.pitest.sequence.Match;
import org.pitest.sequence.QueryParams;
import org.pitest.sequence.QueryStart;
import org.pitest.sequence.SequenceMatcher;
import org.pitest.sequence.Slot;
import org.pitest.sequence.SlotRead;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.pitest.bytecode.analysis.InstructionMatchers.anyInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.isA;
import static org.pitest.bytecode.analysis.InstructionMatchers.isInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.methodCallTo;
import static org.pitest.bytecode.analysis.InstructionMatchers.notAnInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.opCode;

/**
 * Filters out mutants of the form
 *    private aMethod() {
 *      .. clever logic ..
 *      return Stream.empty() -> return null
 *    }
 *
 * Iff the method is only ever called from flatMap.
 *
 * Flat map treats null and Stream.empty as equivalent, but forcing the programmer
 * to return null if they wish to kill the mutant would be controversial at best.
 */
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

    private static final Slot<Location> METHOD_DESC = Slot.create(Location.class);
    static final SequenceMatcher<AbstractInsnNode> HAS_FLAT_MAP_CALL = QueryStart
            .any(AbstractInsnNode.class)
            .then(dynamicCallTo(METHOD_DESC.read()))
            .then(methodCallTo(ClassName.fromClass(Stream.class), "flatMap"))
            .zeroOrMore(QueryStart.match(anyInstruction()))
            .compile(QueryParams.params(AbstractInsnNode.class)
                    .withIgnores(notAnInstruction().or(isA(LabelNode.class)))
                    .withDebug(DEBUG)
            );

    private ClassTree currentClass;
    private Map<Location,Boolean> calledOnlyByFlatMap;

    @Override
    public InterceptorType type() {
        return InterceptorType.FILTER;
    }

    @Override
    public void begin(ClassTree clazz) {
        currentClass = clazz;
        calledOnlyByFlatMap = new HashMap<>();
    }

    @Override
    public Collection<MutationDetails> intercept(Collection<MutationDetails> mutations, Mutater unused) {
        return mutations.stream()
                .filter(m -> !this.isStreamEmptyMutantWithOnlyFlatMapCalls(m))
                .collect(Collectors.toList());
    }

    private boolean isStreamEmptyMutantWithOnlyFlatMapCalls(MutationDetails mutationDetails) {
        if (!mutationDetails.getMutator().equals(NullReturnValsMutator.NULL_RETURNS.getGloballyUniqueId())) {
            return false;
        }

        MethodTree method = currentClass.method(mutationDetails.getId().getLocation()).get();
        if (!method.isPrivate() || !method.returns(ClassName.fromClass(Stream.class))) {
            return false;
        }

        final Context<AbstractInsnNode> context = Context.start(method.instructions(), DEBUG);
        context.store(MUTATED_INSTRUCTION.write(), method.instruction(mutationDetails.getInstructionIndex()));
        return RETURN_EMPTY_STREAM.matches(method.instructions(), context)
                && calledOnlyByFlatMap.computeIfAbsent(mutationDetails.getId().getLocation(), this::calledOnlyFromFlatMap);
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
                .anyMatch(callTo(target.asLocation().getClassName(), target.asLocation().getMethodName(), target.asLocation().getMethodDesc())
                        .or(dynamicCallTo(target.asLocation())));
    }

    private Predicate<AbstractInsnNode> callTo(ClassName className, String name, String desc) {
        return n -> {
            if (n instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) n;
                return call.owner.equals(className.asInternalName()) && call.name.equals(name) && call.desc.equals(desc);
            }
            return false;
        };
    }

    private boolean isFlatMapCall(MethodTree mutated, MethodTree each) {
        final Context<AbstractInsnNode> context = Context.start(each.instructions(), DEBUG);
        context.store(METHOD_DESC.write(), mutated.asLocation());
        return HAS_FLAT_MAP_CALL.matches(each.instructions(), context);
    }


    private static Match<AbstractInsnNode> dynamicCallTo(SlotRead<Location> desc) {
        return (c, t) -> dynamicCallTo(c.retrieve(desc).get()).test(t);
    }

    private static Predicate<AbstractInsnNode> dynamicCallTo(Location desc) {
        return (t) -> {
            if ( t instanceof InvokeDynamicInsnNode) {
                InvokeDynamicInsnNode call = (InvokeDynamicInsnNode) t;
                return Arrays.stream(call.bsmArgs)
                        .anyMatch(isHandle(desc.getClassName(), desc.getMethodName(), desc.getMethodDesc()));
            }
            return false;
        };
    }

    private static Predicate<Object> isHandle(ClassName owner, String name, String desc) {
        return o -> {
            if (o instanceof Handle) {
                Handle handle = (Handle) o;
                return handle.getOwner().equals(owner.asInternalName())
                        && handle.getName().equals(name)
                        && handle.getDesc().equals(desc);
            }
            return false;
        };
    }

    @Override
    public void end() {
        currentClass = null;
        calledOnlyByFlatMap = null;
    }
}
