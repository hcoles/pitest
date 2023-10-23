package org.pitest.mutationtest.build.intercept.lombok;

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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import java.util.stream.Collectors;

import static org.pitest.bytecode.analysis.InstructionMatchers.anyInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.isA;
import static org.pitest.bytecode.analysis.InstructionMatchers.ldcString;
import static org.pitest.bytecode.analysis.InstructionMatchers.newCall;
import static org.pitest.bytecode.analysis.InstructionMatchers.notAnInstruction;
import static org.pitest.bytecode.analysis.OpcodeMatchers.ALOAD;
import static org.pitest.bytecode.analysis.OpcodeMatchers.ATHROW;
import static org.pitest.bytecode.analysis.OpcodeMatchers.DUP;
import static org.pitest.bytecode.analysis.OpcodeMatchers.IFNONNULL;
import static org.pitest.bytecode.analysis.OpcodeMatchers.INVOKESPECIAL;
import static org.pitest.sequence.Result.result;

public class LombokNullFilter extends RegionInterceptor {

    static final Slot<AbstractInsnNode> START = Slot.create(AbstractInsnNode.class);
    static final Slot<AbstractInsnNode> END = Slot.create(AbstractInsnNode.class);

    static final SequenceMatcher<AbstractInsnNode> NULL_CHECK = QueryStart
            .any(AbstractInsnNode.class)
            .then(ALOAD.and(store(START.write())))
            .then(IFNONNULL)
            .then(newCall(ClassName.fromClass(NullPointerException.class)))
            .then(DUP)
            // perhaps requiring the string here is too brittle? But makes it less likely to pick up non lombok generated null checks
            .then(ldcString(s -> s.endsWith("is marked non-null but is null")))
            .then(INVOKESPECIAL)
            .then(ATHROW.and(store(END.write())))
            .zeroOrMore(QueryStart.match(anyInstruction()))
            .compile(QueryParams.params(AbstractInsnNode.class)
                    .withIgnores(notAnInstruction().or(isA(LabelNode.class)))
            );

    private static Match<AbstractInsnNode> store(SlotWrite<AbstractInsnNode> slot) {
        return (c,n) -> result(true, c.store(slot, n));
    }


    protected List<Region> computeRegions(MethodTree method) {
        // Should really be checking that the null check if for one
        // of the annotated parameters, but can likely get away with
        // this since hand rolled nulls checks are not commonly mixed with
        // lombok code
        if (!hasLombokNonNullAnnotation(method)) {
            return Collections.emptyList();
        }

        Context context = Context.start();
        return NULL_CHECK.contextMatches(method.instructions(), context).stream()
                .map(c -> new Region(c.retrieve(START.read()).get(), c.retrieve(END.read()).get()))
                .collect(Collectors.toList());
    }

    private boolean hasLombokNonNullAnnotation(MethodTree method) {
        if (method.rawNode().invisibleParameterAnnotations == null) {
            return false;
        }

        return Arrays.stream(method.rawNode().invisibleParameterAnnotations)
                .filter( l -> l != null) // asm is nasty
                .flatMap(l -> l.stream().filter(node -> node.desc.equals("Llombok/NonNull;")))
                .findAny()
                .isPresent();
    }
}
