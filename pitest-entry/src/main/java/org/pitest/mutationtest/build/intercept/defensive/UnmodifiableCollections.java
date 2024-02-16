package org.pitest.mutationtest.build.intercept.defensive;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.build.intercept.Region;
import org.pitest.mutationtest.build.intercept.RegionInterceptor;
import org.pitest.sequence.Context;
import org.pitest.sequence.Match;
import org.pitest.sequence.QueryParams;
import org.pitest.sequence.QueryStart;
import org.pitest.sequence.SequenceMatcher;
import org.pitest.sequence.Slot;
import org.pitest.sequence.SlotWrite;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.pitest.bytecode.analysis.InstructionMatchers.anyInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.isA;
import static org.pitest.bytecode.analysis.InstructionMatchers.methodCallTo;
import static org.pitest.bytecode.analysis.InstructionMatchers.notAnInstruction;
import static org.pitest.bytecode.analysis.OpcodeMatchers.ARETURN;
import static org.pitest.bytecode.analysis.OpcodeMatchers.INVOKESTATIC;
import static org.pitest.bytecode.analysis.OpcodeMatchers.PUTFIELD;
import static org.pitest.sequence.Result.result;

public class UnmodifiableCollections extends RegionInterceptor {

    static final Slot<AbstractInsnNode> MUTATED_INSTRUCTION = Slot.create(AbstractInsnNode.class);

    static final SequenceMatcher<AbstractInsnNode> DEFENSIVE_RETURN = QueryStart
            .any(AbstractInsnNode.class)
            .then(INVOKESTATIC.and(methodCallTo(ClassName.fromClass(Collections.class), n -> n.startsWith("unmodifiable"))).and(store(MUTATED_INSTRUCTION.write())))
            .then(ARETURN.or(PUTFIELD))
            .zeroOrMore(QueryStart.match(anyInstruction()))
            .compile(QueryParams.params(AbstractInsnNode.class)
                    .withIgnores(notAnInstruction().or(isA(LabelNode.class)))
            );


    @Override
    protected List<Region> computeRegions(MethodTree method) {
        Context context = Context.start();
        return DEFENSIVE_RETURN.contextMatches(method.instructions(), context).stream()
                .map(c -> c.retrieve(MUTATED_INSTRUCTION.read()).get())
                .map(n -> new Region(n, n))
                .collect(Collectors.toList());
    }

    private static Match<AbstractInsnNode> store(SlotWrite<AbstractInsnNode> slot) {
        return (c,n) -> result(true, c.store(slot, n));
    }
}
