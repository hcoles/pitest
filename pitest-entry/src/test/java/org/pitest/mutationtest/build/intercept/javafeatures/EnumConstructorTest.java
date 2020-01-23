package org.pitest.mutationtest.build.intercept.javafeatures;

import org.junit.Test;
import org.pitest.mutationtest.engine.MutationDetails;
import java.util.function.Predicate;

import static org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator.VOID_METHOD_CALL_MUTATOR;

public class EnumConstructorTest {

    EnumConstructorFilter testee = new EnumConstructorFilter();

    FilterTester verifier = new FilterTester("unused", this.testee, VOID_METHOD_CALL_MUTATOR);

    @Test
    public void filtersMutantsFromEnumConstructor() {
        this.verifier.assertFiltersMutationsMatching(inMethodNamed("<init>"), AnEnum.class);
    }

    @Test
    public void doesNotFilterMutantsInCustomEnumMethods() {
        this.verifier.assertFiltersNoMutationsMatching(inMethodNamed("aMethod"), AnEnum.class);
    }

    @Test
    public void doesNotFilterMutantsInNonEnumConstructors() {
        this.verifier.assertFiltersNoMutationsMatching(inMethodNamed("<init>"), AClass.class);
    }

    private Predicate<MutationDetails> inMethodNamed(String name) {
        return m -> m.getMethod().name().equals(name);
    }
}

class AClass {
    AClass(String s) {
        System.out.println(s);
    }
}
