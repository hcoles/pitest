package org.pitest.mutationtest.build.intercept.javafeatures;

import org.junit.Test;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.mutators.NullMutateEverything;
import java.util.function.Predicate;

public class EnumFilterTest {

    EnumFilter testee = new EnumFilter();

    FilterTester verifier = new FilterTester("unused", this.testee, new NullMutateEverything());


    @Test
    public void filtersMutantsFromEnumConstructor() {
        this.verifier.assertFiltersMutationsMatching(inMethodNamed("<init>"), AnEnum.class);
    }

    @Test
    public void filtersMutantsInValueOfMethod() {
        this.verifier.assertFiltersMutationsMatching(inMethodNamed("valueOf"), AnEnum.class);
    }

    @Test
    public void filtersMutantsInValuesMethod() {
        this.verifier.assertFiltersMutationsMatching(inMethodNamed("values"), AnEnum.class);
    }

    @Test
    public void doesNotFilterMutantsInCustomEnumMethods() {
        this.verifier.assertFiltersNoMutationsMatching(inMethodNamed("aMethod"), AnEnum.class);
    }

    @Test
    public void filterMutantsInCustomEnumConstructors() {
        this.verifier.assertFiltersMutationsMatching(inMethodNamed("<init>"), EnumWithCustomConstructor.class);
    }

    @Test
    public void doesNotFilterMutantsInNonEnumConstructors() {
        this.verifier.assertFiltersNoMutationsMatching(inMethodNamed("<init>"), AClass.class);
    }


    private Predicate<MutationDetails> inMethodNamed(String name) {
        return m -> m.getMethod().equals(name);
    }
}

class AClass {
    AClass(String s) {
        System.out.println(s);
    }
}

enum EnumWithCustomConstructor {
    Foo, Bar;

    int i;

    EnumWithCustomConstructor() {
        this.i++;
    }

}