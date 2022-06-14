package org.pitest.mutationtest.build.intercept.javafeatures;

import org.junit.Test;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.gregor.mutators.NullMutateEverything;
import org.pitest.verifier.interceptors.InterceptorVerifier;
import org.pitest.verifier.interceptors.VerifierStart;

import static org.pitest.bytecode.analysis.InstructionMatchers.methodCallTo;
import static org.pitest.bytecode.analysis.OpcodeMatchers.GETSTATIC;
import static org.pitest.bytecode.analysis.OpcodeMatchers.LOOKUPSWITCH;
import static org.pitest.bytecode.analysis.OpcodeMatchers.TABLESWITCH;

public class EnumSwitchTest {

    EnumSwitchFilterFactory underTest = new EnumSwitchFilterFactory();
    InterceptorVerifier v = VerifierStart.forInterceptorFactory(underTest)
            .usingMutator(new NullMutateEverything());

    @Test
    public void doesNotFilterCodeWithNoEnumSwitch() {
        v.forClass(NormalSwitch.class)
                .forAnyCode()
                .mutantsAreGenerated()
                .noMutantsAreFiltered()
                .verify();
    }

    @Test
    public void filtersOrdinalCall() {
        v.forClass(HasEnumTableSwitch.class)
                .forCodeMatching(methodCallTo(ClassName.fromClass(Letters.class), "ordinal").asPredicate())
                .mutantsAreGenerated()
                .allMutantsAreFiltered()
                .verify();
    }

    @Test
    public void filtersFieldAccess() {
        v.forClass(HasEnumTableSwitch.class)
                .forCodeMatching(GETSTATIC.asPredicate())
                .mutantsAreGenerated()
                .allMutantsAreFiltered()
                .verify();
    }

    @Test
    public void leavesTableSwitchStatement() {
        v.forClass(HasEnumTableSwitch.class)
                .forCodeMatching(TABLESWITCH.asPredicate())
                .mutantsAreGenerated()
                .noMutantsAreFiltered()
                .verify();
    }

    @Test
    public void filtersOrdinalCallWhenLookupSwitch() {
        v.forClass(HasEnumLookupSwitch.class)
                .forCodeMatching(methodCallTo(ClassName.fromClass(Sides.class), "ordinal").asPredicate())
                .mutantsAreGenerated()
                .allMutantsAreFiltered()
                .verify();
    }

    @Test
    public void leavesLookupSwitchStatement() {
        v.forClass(HasEnumLookupSwitch.class)
                .forCodeMatching(LOOKUPSWITCH.asPredicate())
                .mutantsAreGenerated()
                .noMutantsAreFiltered()
                .verify();
    }
}

class NormalSwitch {
    int foo(int i) {
        switch (i) {
            case 1:
                return 1;
            case 2:
            case 3:
                return 2;
            default:
                return 0;
        }
    }
}

enum Letters {
    A,B,C,D,E,F;
}

class HasEnumTableSwitch {
    int foo(Letters i) {
        switch (i) {
            case A:
                return 1;
            case B:
            case E:
                return 2;
            default:
                return 0;
        }
    }
}

class HasEnumLookupSwitch {
    int foo(Sides i) {
        switch (i) {
            case UP:
                return 1;
            case DOWN:
                return 0;
        }
        return 2;
    }
}

enum Sides {
    UP, DOWN;
}