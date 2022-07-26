package org.pitest.mutationtest.build.intercept.equivalent;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.build.intercept.Region;
import org.pitest.mutationtest.build.intercept.RegionInterceptor;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.mutators.returns.BooleanFalseReturnValsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.returns.EmptyObjectReturnValsMutator;
import org.pitest.sequence.Context;
import org.pitest.sequence.Match;
import org.pitest.sequence.QueryParams;
import org.pitest.sequence.QueryStart;
import org.pitest.sequence.SequenceMatcher;
import org.pitest.sequence.SequenceQuery;
import org.pitest.sequence.Slot;
import org.pitest.sequence.SlotWrite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.pitest.bytecode.analysis.InstructionMatchers.aVariableAccess;
import static org.pitest.bytecode.analysis.InstructionMatchers.anyInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.getStatic;
import static org.pitest.bytecode.analysis.InstructionMatchers.isA;
import static org.pitest.bytecode.analysis.InstructionMatchers.methodCallNamed;
import static org.pitest.bytecode.analysis.InstructionMatchers.methodCallTo;
import static org.pitest.bytecode.analysis.InstructionMatchers.notAnInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.variableMatches;
import static org.pitest.bytecode.analysis.OpcodeMatchers.ALOAD;
import static org.pitest.bytecode.analysis.OpcodeMatchers.ARETURN;
import static org.pitest.bytecode.analysis.OpcodeMatchers.ASTORE;
import static org.pitest.bytecode.analysis.OpcodeMatchers.LDC;
import static org.pitest.sequence.Result.result;

/**
 * Handles methods already returning a 0 value, and also
 * those returning Boolean.FALSE
 */
class EmptyReturnsFilter extends RegionInterceptor {

    private static final Slot<AbstractInsnNode> MUTATED_INSTRUCTION = Slot.create(AbstractInsnNode.class);
    private static final Slot<Integer> LOCAL_VAR = Slot.create(Integer.class);
    private static final Set<String> MUTATOR_IDS = new HashSet<>();

    static {
        MUTATOR_IDS.add(EmptyObjectReturnValsMutator.EMPTY_RETURNS.getGloballyUniqueId());
        MUTATOR_IDS.add(BooleanFalseReturnValsMutator.FALSE_RETURNS.getGloballyUniqueId());
    }

    private final SequenceMatcher<AbstractInsnNode> zeroValues =
            directValues().or(inDirectValues())
                    .compile(QueryParams.params(AbstractInsnNode.class)
                            .withIgnores(notAnInstruction().or(isA(LabelNode.class)))
                    );

    private SequenceQuery<AbstractInsnNode> directValues() {
        return QueryStart
                .any(AbstractInsnNode.class)
                .zeroOrMore(QueryStart.match(anyInstruction()))
                .then(matches())
                .then(ARETURN.and(store(MUTATED_INSTRUCTION.write())))
                .zeroOrMore(QueryStart.match(anyInstruction()));
    }

    private SequenceQuery<AbstractInsnNode> inDirectValues() {
        return QueryStart
                .any(AbstractInsnNode.class)
                .zeroOrMore(QueryStart.match(anyInstruction()))
                .then(matches())
                .then(aStoreTo(LOCAL_VAR))
                // match anything that doesn't overwrite the local var
                // possible we will get issues here if there is a jump instruction
                // to get to the point that the empty value is returned.
                .zeroOrMore(QueryStart.match(ASTORE.and(variableMatches(LOCAL_VAR.read())).negate()))
                .then(ALOAD.and(variableMatches(LOCAL_VAR.read())))
                .then(ARETURN.and(store(MUTATED_INSTRUCTION.write())))
                .zeroOrMore(QueryStart.match(anyInstruction()));
    }

    private SequenceQuery<AbstractInsnNode> matches() {
      return constantZero().or(constantFalse()).or(emptyString()).or(QueryStart.match(loadsEmptyReturnOntoStack()));
    }

    private SequenceQuery<AbstractInsnNode> constantZero() {
        return QueryStart
                .match(isZeroConstant())
                .then(methodCallNamed("valueOf"));
    }

    private SequenceQuery<AbstractInsnNode> constantFalse() {
        return QueryStart
                .match(getStatic("java/lang/Boolean","FALSE"));
    }

    private SequenceQuery<AbstractInsnNode> emptyString() {
        return QueryStart
                .match(LDC.and(ldcConstant("")));
    }

    private static Match<AbstractInsnNode> ldcConstant(String s) {
        return (c,n) -> result(s.equals(((LdcInsnNode) n).cst), c);
    }


    private static Match<AbstractInsnNode> store(SlotWrite<AbstractInsnNode> slot) {
        return (c, n) -> result(true, c.store(slot, n));
    }


    @Override
    protected List<Region> computeRegions(MethodTree method) {
        Context context = Context.start();
        return zeroValues.contextMatches(method.instructions(), context).stream()
                .map(c -> new Region(c.retrieve(MUTATED_INSTRUCTION.read()).get(), c.retrieve(MUTATED_INSTRUCTION.read()).get()))
                .collect(Collectors.toList());
    }


    @Override
    public Collection<MutationDetails> intercept(
            Collection<MutationDetails> mutations, Mutater unused) {

        List<MutationDetails> targets = mutations.stream()
                .filter(m -> MUTATOR_IDS.contains(m.getMutator()))
                .collect(Collectors.toList());

        // performance hack. Avoid class analysis if no relevent matches
        if (targets.isEmpty()) {
            return mutations;
        }

        List<MutationDetails> toReturn = new ArrayList<>(mutations);
        toReturn.removeAll(targets);
        toReturn.addAll(super.intercept(targets, unused));

        return toReturn;
    }

    private static Match<AbstractInsnNode> aStoreTo(Slot<Integer> variable) {
        return ASTORE.and(aVariableAccess(variable.write()));
    }

    private static Match<AbstractInsnNode> isZeroConstant() {
        Set<Integer> zeroConstants = new HashSet<>();

            zeroConstants.add(Opcodes.ICONST_0);
            zeroConstants.add(Opcodes.LCONST_0);
            zeroConstants.add(Opcodes.FCONST_0);
            zeroConstants.add(Opcodes.DCONST_0);

        return (context,node) -> result(zeroConstants.contains(node.getOpcode()), context);
    }

    private static Match<AbstractInsnNode> loadsEmptyReturnOntoStack() {
        return noArgsCall("java/util/Optional", "empty")
                .or(noArgsCall("java/util/stream/Stream", "empty"))
                .or(noArgsCall("java/util/Collections", "emptyList"))
                .or(noArgsCall("java/util/Collections", "emptyMap"))
                .or(noArgsCall("java/util/Collections", "emptySet"))
                .or(noArgsCall("java/util/List", "of"))
                .or(noArgsCall("java/util/Set", "of"));
    }

    private static Match<AbstractInsnNode> noArgsCall(String owner, String name) {
        return methodCallTo(ClassName.fromString(owner), name).and(takesNoArgs());
    }

    private static Match<AbstractInsnNode> takesNoArgs() {
        return (c,node) -> {
            if (node instanceof MethodInsnNode) {
                final MethodInsnNode call = (MethodInsnNode) node;
                return result(Type.getArgumentTypes(call.desc).length == 0, c);
            }
            return result(false, c);
        };
    }


}