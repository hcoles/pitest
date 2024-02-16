package org.pitest.mutationtest.build.intercept.defensive;

import org.junit.Test;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.mutationtest.engine.gregor.mutators.NullMutateEverything;
import org.pitest.verifier.interceptors.FactoryVerifier;
import org.pitest.verifier.interceptors.InterceptorVerifier;
import org.pitest.verifier.interceptors.VerifierStart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.pitest.bytecode.analysis.OpcodeMatchers.INVOKESTATIC;


public class UnmodifiableCollectionsFactoryTest {
    private final MutationInterceptorFactory underTest = new UnmodifiableCollectionFactory();
    InterceptorVerifier v = VerifierStart.forInterceptorFactory(underTest)
            .usingMutator(new NullMutateEverything());

    @Test
    public void isOnChain() {
        FactoryVerifier.confirmFactory(underTest)
                .isOnChain();
    }

    @Test
    public void isOffByDefault() {
        FactoryVerifier.confirmFactory(underTest)
                .isOffByDefault();
    }

    @Test
    public void featureIsCalledUnmodifiableCollection() {
        FactoryVerifier.confirmFactory(underTest)
                .featureName().isEqualTo("funmodifiablecollection");
    }

    @Test
    public void createsFilters() {
        FactoryVerifier.confirmFactory(underTest)
                .createsInterceptorsOfType(InterceptorType.FILTER);
    }


    @Test
    public void filtersMutationsToReturnUnmodifiableSet() {
        v.forClass(HasUnmodifiableSetReturn.class)
                .forCodeMatching(INVOKESTATIC.asPredicate())
                .allMutantsAreFiltered()
                .verify();
    }

    @Test
    public void filtersMutationsToReturnUnmodifiableList() {
        v.forClass(HasUnmodifiableListReturn.class)
                .forCodeMatching(INVOKESTATIC.asPredicate())
                .allMutantsAreFiltered()
                .verify();
    }

    @Test
    public void filtersMutationsToReturnUnmodifiableMap() {
        v.forClass(HasUnmodifiableMapReturn.class)
                .forCodeMatching(INVOKESTATIC.asPredicate())
                .allMutantsAreFiltered()
                .verify();
    }

    @Test
    public void doesNotFilterOtherCode() {
        v.forClass(HasUnmodifiableSetReturn.class)
                .forCodeMatching(INVOKESTATIC.asPredicate().negate())
                .noMutantsAreFiltered()
                .verify();
    }

    @Test
    public void filtersUnmodifiableStoresToFields() {
        v.forClass(StoresToFields.class)
                .forCodeMatching(INVOKESTATIC.asPredicate())
                .allMutantsAreFiltered()
                .verify();
    }

    @Test
    public void doesNotFilterOtherCallsToUnModifiableSet() {
        v.forClass(HasUnmodifiableSetNonReturn.class)
                .forAnyCode()
                .noMutantsAreFiltered()
                .verify();
    }
}

class StoresToFields {
    final List<String> ls;

    StoresToFields(List<String> mod) {
        ls = Collections.unmodifiableList(mod);
    }
}

class HasUnmodifiableSetReturn {
    private final Set<String> s = new HashSet<>();

    public Set<String> mutateMe(int i) {
        if (i != 1) {
            return Collections.unmodifiableSet(s);
        }

        return s;
    }
}

class HasUnmodifiableListReturn {
    private final List<String> s = new ArrayList<>();

    public List<String> mutateMe(int i) {
        if (i != 1) {
            return Collections.unmodifiableList(s);
        }

        return s;
    }
}

class HasUnmodifiableMapReturn {

    public Map<String,String> mutateMe(Map<String,String> m) {
        return Collections.unmodifiableMap(m);
    }
}

class HasUnmodifiableSetNonReturn {
    private final Set<String> s = new HashSet<>();

    public Set<String> dontMutateME(int i) {
        if (i != 1) {
            Set<String> copy = Collections.unmodifiableSet(s);
        }

        return s;
    }
}