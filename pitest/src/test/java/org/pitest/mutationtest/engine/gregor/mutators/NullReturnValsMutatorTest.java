package org.pitest.mutationtest.engine.gregor.mutators;

import org.junit.Test;
import org.pitest.verifier.mutants.MutatorVerifierStart;

import java.util.concurrent.Callable;

import static org.pitest.mutationtest.engine.gregor.mutators.returns.NullReturnValsMutator.NULL_RETURNS;

@interface SomethingElse {

}

@interface NotNull {

}

public class NullReturnValsMutatorTest {

    MutatorVerifierStart v = MutatorVerifierStart.forMutator(NULL_RETURNS)
            .notCheckingUnMutatedValues();

    @Test
    public void mutatesObjectReturnValuesToNull() {
        v.forCallableClass(ObjectReturn.class)
                .firstMutantShouldReturn(null);
    }

    @Test
    public void doesNotMutateMethodsAnnotatedWithNotNull() {
        v.forCallableClass(
                        AnnotatedObjectReturn.class)
                .noMutantsCreated();
    }

    @Test
    public void mutatesMethodsWithOtherAnnoations() {
        v.forCallableClass(
                        HasOtherAnnotation.class)
                .createsNMutants(1);
    }


    @Test
    public void doesNotMutateMethodsAnnotatedWithNotNullAndOthers() {
        v.forCallableClass(
                        MultipleAnnotatedObjectReturn.class)
                .noMutantsCreated();
    }


    @Test
    public void describesMutationsToObject() {
        v.forCallableClass(ObjectReturn.class)
                .firstMutantDescription()
                .contains("replaced return value with null")
                .contains("ObjectReturn::call");
    }

    private static class ObjectReturn implements Callable<Object> {
        @Override
        public Object call() {
            return "";
        }
    }

    private static class AnnotatedObjectReturn implements Callable<Object> {
        @Override
        @NotNull
        public Object call() {
            return "";
        }
    }

    private static class HasOtherAnnotation implements Callable<Object> {
        @Override
        @SomethingElse
        public Object call() {
            return "";
        }
    }

    private static class MultipleAnnotatedObjectReturn implements Callable<Object> {
        @Override
        @NotNull
        @SomethingElse
        public Object call() {
            return "";
        }
    }
}
