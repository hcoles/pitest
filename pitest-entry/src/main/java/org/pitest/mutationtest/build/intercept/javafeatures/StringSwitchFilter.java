package org.pitest.mutationtest.build.intercept.javafeatures;

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
import org.pitest.sequence.SequenceQuery;
import org.pitest.sequence.Slot;
import org.pitest.sequence.SlotWrite;

import java.util.List;
import java.util.stream.Collectors;

import static org.pitest.bytecode.analysis.InstructionMatchers.anIntegerConstant;
import static org.pitest.bytecode.analysis.InstructionMatchers.anyInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.isA;
import static org.pitest.bytecode.analysis.InstructionMatchers.methodCallTo;
import static org.pitest.bytecode.analysis.InstructionMatchers.notAnInstruction;
import static org.pitest.bytecode.analysis.OpcodeMatchers.ALOAD;
import static org.pitest.bytecode.analysis.OpcodeMatchers.BIPUSH;
import static org.pitest.bytecode.analysis.OpcodeMatchers.GOTO;
import static org.pitest.bytecode.analysis.OpcodeMatchers.ICONST_M1;
import static org.pitest.bytecode.analysis.OpcodeMatchers.IFEQ;
import static org.pitest.bytecode.analysis.OpcodeMatchers.ILOAD;
import static org.pitest.bytecode.analysis.OpcodeMatchers.ISTORE;
import static org.pitest.bytecode.analysis.OpcodeMatchers.LDC;
import static org.pitest.bytecode.analysis.OpcodeMatchers.LOOKUPSWITCH;
import static org.pitest.bytecode.analysis.OpcodeMatchers.SIPUSH;
import static org.pitest.bytecode.analysis.OpcodeMatchers.TABLESWITCH;
import static org.pitest.sequence.Result.result;

public class StringSwitchFilter extends RegionInterceptor {

    static final Slot<AbstractInsnNode> START = Slot.create(AbstractInsnNode.class);
    static final Slot<AbstractInsnNode> END = Slot.create(AbstractInsnNode.class);

    static final SequenceMatcher<AbstractInsnNode> STRING_SWITCH = QueryStart
            .any(AbstractInsnNode.class)
            .then(ICONST_M1.and(store(START.write())))
            .then(ISTORE)
            .then(ALOAD)
            .then(methodCallTo(ClassName.fromClass(String.class), "hashCode"))
            .then(LOOKUPSWITCH.or(TABLESWITCH))
            .then(isA(LabelNode.class))
            .oneOrMore(switchBranchSequence().then(isA(LabelNode.class)))
            .then(switchBranchSequenceNoGoto())
            .then(isA(LabelNode.class))
            .then(ILOAD.and(store(END.write())))
            .then(LOOKUPSWITCH.or(TABLESWITCH))
            .zeroOrMore(QueryStart.match(anyInstruction()))
            .compile(QueryParams.params(AbstractInsnNode.class)
                    .withIgnores(notAnInstruction())
            );

    private static Match<AbstractInsnNode> store(SlotWrite<AbstractInsnNode> slot) {
        return (c,n) -> result(true, c.store(slot, n));
    }

    private static SequenceQuery<AbstractInsnNode> switchBranchSequenceNoGoto() {
        return QueryStart.match(ALOAD)
                .then(LDC)
                .then(methodCallTo(ClassName.fromClass(String.class), "equals"))
                .then(IFEQ)
                .then(anIntegerConstant().or(BIPUSH).or(SIPUSH))
                .then(ISTORE);
    }

    private static SequenceQuery<AbstractInsnNode> switchBranchSequence() {
        return switchBranchSequenceNoGoto()
                .then(GOTO);
    }


    protected List<Region> computeRegions(MethodTree method) {
        Context context = Context.start();
        return STRING_SWITCH.contextMatches(method.instructions(), context).stream()
                .map(c -> new Region(c.retrieve(START.read()).get(), c.retrieve(END.read()).get()))
                .collect(Collectors.toList());
    }

}