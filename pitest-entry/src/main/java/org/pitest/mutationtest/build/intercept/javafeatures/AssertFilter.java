package org.pitest.mutationtest.build.intercept.javafeatures;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.mutationtest.build.intercept.Region;
import org.pitest.mutationtest.build.intercept.RegionInterceptor;
import org.pitest.sequence.Context;
import org.pitest.sequence.Match;
import org.pitest.sequence.QueryParams;
import org.pitest.sequence.QueryStart;
import org.pitest.sequence.SequenceMatcher;
import org.pitest.sequence.Slot;
import org.pitest.sequence.SlotWrite;

import java.util.List;
import java.util.stream.Collectors;

import static org.pitest.bytecode.analysis.InstructionMatchers.anyInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.jumpsTo;
import static org.pitest.bytecode.analysis.InstructionMatchers.notAnInstruction;
import static org.pitest.bytecode.analysis.OpcodeMatchers.IFNE;
import static org.pitest.sequence.Result.result;


public class AssertFilter extends RegionInterceptor {

    static final Slot<AbstractInsnNode> START = Slot.create(AbstractInsnNode.class);
    static final Slot<LabelNode> END = Slot.create(LabelNode.class);

    static final SequenceMatcher<AbstractInsnNode> ASSERT_GET = QueryStart
            .any(AbstractInsnNode.class)
            .then(getStatic("$assertionsDisabled").and(store(START.write())))
            .then(IFNE.and(jumpsTo(END.write())))
            .zeroOrMore(QueryStart.match(anyInstruction()))
            .compile(QueryParams.params(AbstractInsnNode.class)
                    .withIgnores(notAnInstruction())
            );

    private static Match<AbstractInsnNode> getStatic(String name) {
        return (c, n) -> {
            if (n instanceof FieldInsnNode) {
                return result(((FieldInsnNode) n).name.equals(name), c);
            }
            return result(false, c);
        };
    }


    private static Match<AbstractInsnNode> store(SlotWrite<AbstractInsnNode> slot) {
        return (c, n) -> result(true, c.store(slot, n));
    }

    protected List<Region> computeRegions(MethodTree method) {
        Context context = Context.start();
        return ASSERT_GET.contextMatches(method.instructions(), context).stream()
                .map(c -> new Region(c.retrieve(START.read()).get(), c.retrieve(END.read()).get()))
                .collect(Collectors.toList());
    }

}
