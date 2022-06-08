package org.pitest.mutationtest.build.intercept.javafeatures;

import org.junit.Ignore;
import org.junit.Test;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.gregor.mutators.NullMutateEverything;
import org.pitest.verifier.interceptors.InterceptorVerifier;
import org.pitest.verifier.interceptors.VerifierStart;

import static org.pitest.bytecode.analysis.InstructionMatchers.methodCallTo;
import static org.pitest.bytecode.analysis.OpcodeMatchers.ARETURN;

public class StringSwitchTest {

    StringSwitchFilterFactory underTest = new StringSwitchFilterFactory();
    InterceptorVerifier v = VerifierStart.forInterceptorFactory(underTest)
            .usingMutator(new NullMutateEverything());

    @Test
    @Ignore
    public void filtersStringSwitchHashcode() {
        v.forClass(StringSwitch.class)
                .forCodeMatching( methodCallTo(ClassName.fromClass(String.class), "hashCode").asPredicate() )
                .mutantsAreGenerated()
                .allMutantsAreFiltered()
                .verify();
    }

    @Test
    public void doesNotFilterOtherStatements() {
        v.forClass(StringSwitch.class)
                .forCodeMatching( ARETURN.asPredicate() )
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