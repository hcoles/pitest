package org.pitest.mutationtest.build.intercept.javafeatures;

import org.junit.Test;
import org.objectweb.asm.tree.JumpInsnNode;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.gregor.mutators.NullMutateEverything;
import org.pitest.verifier.interceptors.InterceptorVerifier;
import org.pitest.verifier.interceptors.VerifierStart;

import static org.pitest.bytecode.analysis.InstructionMatchers.isA;
import static org.pitest.bytecode.analysis.InstructionMatchers.methodCallTo;
import static org.pitest.bytecode.analysis.OpcodeMatchers.ARETURN;
import static org.pitest.bytecode.analysis.OpcodeMatchers.GOTO;
import static org.pitest.bytecode.analysis.OpcodeMatchers.IADD;

public class StringSwitchTest {

    StringSwitchFilterFactory underTest = new StringSwitchFilterFactory();
    InterceptorVerifier v = VerifierStart.forInterceptorFactory(underTest)
            .usingMutator(new NullMutateEverything());

    @Test
    public void filtersStringSwitchHashcode() {
        v.forClass(StringSwitch.class)
                .forCodeMatching( methodCallTo(ClassName.fromClass(String.class), "hashCode").asPredicate() )
                .mutantsAreGenerated()
                .allMutantsAreFiltered()
                .verify();
    }

    @Test
    public void filtersStringSwitchJumps() {
        v.forClass(StringSwitch.class)
                .forCodeMatching(isA(JumpInsnNode.class).asPredicate())
                .mutantsAreGenerated()
                .allMutantsAreFiltered()
                .verify();
    }

    @Test
    public void doesNotFilterOtherStatements() {
        v.forClass(StringSwitch.class)
                .forCodeMatching(ARETURN.asPredicate())
                .mutantsAreGenerated()
                .noMutantsAreFiltered()
                .verify();
    }

    @Test
    public void filtersStringTableSwitchHashcode() {
        v.forClass(StringTableSwitch.class)
                .forCodeMatching( methodCallTo(ClassName.fromClass(String.class), "hashCode").asPredicate() )
                .mutantsAreGenerated()
                .allMutantsAreFiltered()
                .verify();
    }

    @Test
    public void doesNotFilterOtherTableSwitchStatements() {
        v.forClass(StringSwitch.class)
                .forCodeMatching(ARETURN.asPredicate())
                .mutantsAreGenerated()
                .noMutantsAreFiltered()
                .verify();
    }

    @Test
    public void filtersSmallSideEffectingSwitches() {
        v.forClass(SmallStringSwitch.class)
                .forCodeMatching(isA(JumpInsnNode.class).and(GOTO.negate()).asPredicate() )
                .mutantsAreGenerated()
                .allMutantsAreFiltered()
                .verify();

        v.forClass(SmallStringSwitch.class)
                .forCodeMatching(IADD.asPredicate())
                .mutantsAreGenerated()
                .noMutantsAreFiltered()
                .verify();
    }

    @Test
    public void filtersMultipleSwitchesInSingleMethod() {
        v.forClass(MultipleSwitches.class)
                .forCodeMatching(isA(JumpInsnNode.class).and(GOTO.negate()).asPredicate() )
                .mutantsAreGenerated()
                .allMutantsAreFiltered()
                .verify();

        v.forClass(MultipleSwitches.class)
                .forCodeMatching(IADD.asPredicate())
                .mutantsAreGenerated()
                .noMutantsAreFiltered()
                .verify();
    }

}

class StringSwitch {
    public String target(String in) {
        switch (in) {
            case "Boo":
                return "A";
            case "Coo":
                return "B";
            default:
                return "C";
        }
    }
}

class StringTableSwitch {
    public String target(String in) {
        switch (in) {
            case "AA":
                return "A";
            case "BB":
                return "B";
            case "CC":
                return "C";
            case "DD":
                return "D";
            case "EE":
                return "E";
            case "FF":
                return "F";
            case "GG":
                return "G";
            case "HH":
                return "H";
            case "II":
                return "I";
            case "JJ":
                return "J";
            case "KK":
                return "K";
            case "LL":
                return "L";
            default:
                return "ZZ";
        }
    }
}

class SmallStringSwitch {
    public int target(String in) {
        int a = 0;
        switch (in) {
            case "Boo":
                a = a + 1;
                break;
            case "Coo":
                a = a + 10;
                // fall through
            default:
                a = a + 2;
        }
        return a;
    }
}

class MultipleSwitches {
    public int target(String in) {
        int a = 0;
        switch (in) {
            case "Boo":
                a = a + 1;
                break;
            case "Coo":
                a = a + 10;
                // fall through
            default:
                a = a + 2;
        }

        switch (in) {
            case "A":
                a = a + 1;
                break;
            case "B":
                a = a + 10;
                // fall through
            default:
                a = a + 2;
        }

        switch (in) {
            case "C":
                a = a + 1;
                break;
            case "D":
                a = a + 10;
                // fall through
            default:
                a = a + 2;
        }
        return a;
    }
}