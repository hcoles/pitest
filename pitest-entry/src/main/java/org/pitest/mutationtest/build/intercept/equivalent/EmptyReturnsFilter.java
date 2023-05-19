package org.pitest.mutationtest.build.intercept.equivalent;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.mutationtest.build.intercept.MutatorSpecificInterceptor;
import org.pitest.mutationtest.build.intercept.Region;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.sequence.Context;
import org.pitest.sequence.Match;
import org.pitest.sequence.QueryParams;
import org.pitest.sequence.QueryStart;
import org.pitest.sequence.SequenceMatcher;
import org.pitest.sequence.SequenceQuery;
import org.pitest.sequence.Slot;
import org.pitest.sequence.SlotWrite;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.pitest.bytecode.analysis.InstructionMatchers.aVariableAccess;
import static org.pitest.bytecode.analysis.InstructionMatchers.anyInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.isA;
import static org.pitest.bytecode.analysis.InstructionMatchers.notAnInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.variableMatches;
import static org.pitest.bytecode.analysis.OpcodeMatchers.ALOAD;
import static org.pitest.bytecode.analysis.OpcodeMatchers.ASTORE;
import static org.pitest.sequence.Result.result;

class EmptyReturnsFilter extends MutatorSpecificInterceptor {

    private static final Slot<AbstractInsnNode> AVOID = Slot.create(AbstractInsnNode.class);
    private static final Slot<Integer> LOCAL_VAR = Slot.create(Integer.class);

    private final SequenceQuery<AbstractInsnNode> matches;
    private final SequenceMatcher<AbstractInsnNode> zeroValues;
    private final Match<AbstractInsnNode> returnMatch;

    EmptyReturnsFilter(SequenceQuery<AbstractInsnNode> matches, Match<AbstractInsnNode> returnMatch, MethodMutatorFactory... mutators) {
        super(asList(mutators));

        this.matches = matches;
        this.returnMatch = returnMatch;
        this.zeroValues = directValues().or(inDirectValues())
                .compile(QueryParams.params(AbstractInsnNode.class)
                        .withIgnores(notAnInstruction().or(isA(LabelNode.class)))
                );
    }

    private SequenceQuery<AbstractInsnNode> directValues() {
        return QueryStart
                .any(AbstractInsnNode.class)
                .zeroOrMore(QueryStart.match(anyInstruction()))
                .then(matches)
                .then(returnMatch.and(store(AVOID.write())))
                .zeroOrMore(QueryStart.match(anyInstruction()));
    }

    private SequenceQuery<AbstractInsnNode> inDirectValues() {
        return QueryStart
                .any(AbstractInsnNode.class)
                .zeroOrMore(QueryStart.match(anyInstruction()))
                .then(matches)
                .then(aStoreTo(LOCAL_VAR))
                // match anything that doesn't overwrite the local var
                // possible we will get issues here if there is a jump instruction
                // to get to the point that the empty value is returned.
                .zeroOrMore(QueryStart.match(ASTORE.and(variableMatches(LOCAL_VAR.read())).negate()))
                .then(ALOAD.and(variableMatches(LOCAL_VAR.read())))
                .then(returnMatch.and(store(AVOID.write())))
                .zeroOrMore(QueryStart.match(anyInstruction()));
    }


    private static Match<AbstractInsnNode> store(SlotWrite<AbstractInsnNode> slot) {
        return (c, n) -> result(true, c.store(slot, n));
    }

    @Override
    protected List<Region> computeRegions(MethodTree method) {
        Context context = Context.start();
        return zeroValues.contextMatches(method.instructions(), context).stream()
                .map(c -> new Region(c.retrieve(AVOID.read()).get(), c.retrieve(AVOID.read()).get()))
                .collect(Collectors.toList());
    }

    private static Match<AbstractInsnNode> aStoreTo(Slot<Integer> variable) {
        return ASTORE.and(aVariableAccess(variable.write()));
    }

}