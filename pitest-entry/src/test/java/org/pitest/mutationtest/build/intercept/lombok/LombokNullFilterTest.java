package org.pitest.mutationtest.build.intercept.lombok;

import org.junit.Test;
import org.pitest.bytecode.analysis.OpcodeMatchers;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.mutationtest.engine.gregor.mutators.NullMutateEverything;
import org.pitest.verifier.interceptors.FactoryVerifier;
import org.pitest.verifier.interceptors.InterceptorVerifier;
import org.pitest.verifier.interceptors.VerifierStart;

public class LombokNullFilterTest {

    private MutationInterceptorFactory underTest = new LombokFilter();
    InterceptorVerifier v = VerifierStart.forInterceptorFactory(underTest)
            .usingMutator(new NullMutateEverything());

    @Test
    public void isOnChain() {
        FactoryVerifier.confirmFactory(underTest)
                .isOnChain();
    }

    @Test
    public void isOnByDefault() {
        FactoryVerifier.confirmFactory(underTest)
                .isOnByDefault();
    }

    @Test
    public void featureIsCalledLombok() {
        FactoryVerifier.confirmFactory(underTest)
                .featureName().isEqualTo("lombok");
    }

    @Test
    public void createsFilters() {
        FactoryVerifier.confirmFactory(underTest)
                .createsInterceptorsOfType(InterceptorType.FILTER);
    }

    @Test
    public void filtersMutationsToLombokNotNullCode() {
        v.usingResourceFolder("lombok")
                .forClass("ExampleNotNull")
                .forCodeMatching(OpcodeMatchers.IFNONNULL.asPredicate())
                .mutantsAreGenerated()
                .allMutantsAreFiltered()
                .verify();
    }

    @Test
    public void doesNotFilterHandRolledNullChecks() {
        v.forClass(HandRolledNullChecks.class)
                .forAnyCode()
                .mutantsAreGenerated()
                .noMutantsAreFiltered()
                .verify();
    }
}

class HandRolledNullChecks {
    void mutateMe(String a, String b) {
        if (a == null) {
            throw new NullPointerException("a is marked non-null but is null");
        }
    }
}