package org.pitest.mutationtest.build.intercept.javafeatures;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
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
import static org.pitest.bytecode.analysis.InstructionMatchers.methodCallNamed;
import static org.pitest.bytecode.analysis.InstructionMatchers.notAnInstruction;
import static org.pitest.bytecode.analysis.OpcodeMatchers.ALOAD;
import static org.pitest.bytecode.analysis.OpcodeMatchers.IALOAD;
import static org.pitest.bytecode.analysis.OpcodeMatchers.LOOKUPSWITCH;
import static org.pitest.bytecode.analysis.OpcodeMatchers.TABLESWITCH;
import static org.pitest.sequence.Result.result;

/**
 * For switches on Enums java creates a synthetic class
 * with an int array field. The following code then accesses
 * it
 *
 *  GETSTATIC pkg/Person$1.$SwitchMap$pkg$MyEnum : [I
 *  ALOAD 1
 *  INVOKEVIRTUAL pkg/MyEnum.ordinal ()I
 *  IALOAD
 *  LOOKUPSWITCH
 *
 *  As the generated class is synthetic, no mutants will be
 *  seeded in it. The code that accesses it must however be
 *  filtered.
 */
public class EnumSwitchFilter extends RegionInterceptor {
    static final Slot<AbstractInsnNode> START = Slot.create(AbstractInsnNode.class);
    static final Slot<AbstractInsnNode> END = Slot.create(AbstractInsnNode.class);

    static final SequenceMatcher<AbstractInsnNode> ENUM_SWITCH = QueryStart
            .any(AbstractInsnNode.class)
            .then(getStatic("$SwitchMap$").and(store(START.write())))
            .then(ALOAD)
            .then(methodCallNamed("ordinal"))
            .then(IALOAD.and(store(END.write())))
            .then(LOOKUPSWITCH.or(TABLESWITCH))
            .zeroOrMore(QueryStart.match(anyInstruction()))
            .compile(QueryParams.params(AbstractInsnNode.class)
                    .withIgnores(notAnInstruction())
            );

    private static Match<AbstractInsnNode> getStatic(String name) {
        return (c, n) -> {
            if (n instanceof FieldInsnNode) {
                return result(((FieldInsnNode) n).name.contains(name), c);
            }
            return result(false, c);
        };
    }


    protected List<Region> computeRegions(MethodTree method) {
        Context context = Context.start();
        return ENUM_SWITCH.contextMatches(method.instructions(), context).stream()
                .map(c -> new Region(c.retrieve(START.read()).get(), c.retrieve(END.read()).get()))
                .collect(Collectors.toList());
    }

    private static Match<AbstractInsnNode> store(SlotWrite<AbstractInsnNode> slot) {
        return (c, n) -> result(true, c.store(slot, n));
    }
}
