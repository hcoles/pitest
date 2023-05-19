package org.pitest.mutationtest.build.intercept.equivalent;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.mutationtest.build.intercept.MutatorSpecificInterceptor;
import org.pitest.mutationtest.build.intercept.Region;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.sequence.Context;
import org.pitest.sequence.Match;
import org.pitest.sequence.QueryParams;
import org.pitest.sequence.QueryStart;
import org.pitest.sequence.SequenceMatcher;
import org.pitest.sequence.Slot;
import org.pitest.sequence.SlotWrite;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.pitest.bytecode.analysis.InstructionMatchers.anyInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.notAnInstruction;
import static org.pitest.bytecode.analysis.OpcodeMatchers.DMUL;
import static org.pitest.bytecode.analysis.OpcodeMatchers.FMUL;
import static org.pitest.bytecode.analysis.OpcodeMatchers.ICONST_M1;
import static org.pitest.bytecode.analysis.OpcodeMatchers.IMUL;
import static org.pitest.bytecode.analysis.OpcodeMatchers.LMUL;
import static org.pitest.sequence.Result.result;

/**
 * Filters equivalent mutations of the form
 *
 * (a + b) * -1 -> (a + b) / -1
 *
 */
class DivisionByMinusOneFilter extends MutatorSpecificInterceptor {

    static final Slot<AbstractInsnNode> AVOID = Slot.create(AbstractInsnNode.class);

    static final SequenceMatcher<AbstractInsnNode> DIVISION_BY_1 = QueryStart
            .any(AbstractInsnNode.class)
            .then(ICONST_M1.or(loads(-1L)).or(loads(-1f)).or(loads(-1d)))
            .then(IMUL.or(LMUL.or(FMUL).or(DMUL)).and(store(AVOID.write())))
            .zeroOrMore(QueryStart.match(anyInstruction()))
            .compile(QueryParams.params(AbstractInsnNode.class)
                    .withIgnores(notAnInstruction())
            );

    DivisionByMinusOneFilter(MethodMutatorFactory... mutators) {
        super(asList(mutators));
    }


    private static Match<AbstractInsnNode> loads(Object l) {
        return (c,n) ->
            result(n instanceof LdcInsnNode && ((LdcInsnNode) n).cst.equals(l), c);
    }
    private static Match<AbstractInsnNode> store(SlotWrite<AbstractInsnNode> slot) {
        return (c, n) -> result(true, c.store(slot, n));
    }

    @Override
    protected List<Region> computeRegions(MethodTree method) {
        Context context = Context.start();
        return DIVISION_BY_1.contextMatches(method.instructions(), context).stream()
                .map(c -> new Region(c.retrieve(AVOID.read()).get(), c.retrieve(AVOID.read()).get()))
                .collect(Collectors.toList());
    }

}
