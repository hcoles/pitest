package org.pitest.mutationtest.build.intercept.groovy;

import com.example.coverage.execute.samples.exceptions.CoveredBeforeExceptionTestee;
import org.junit.Test;
import org.pitest.mutationtest.engine.gregor.config.Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.NullMutateEverything;
import org.pitest.util.ResourceFolderByteArraySource;
import org.pitest.verifier.interceptors.InterceptorVerifier;
import org.pitest.verifier.interceptors.VerifierStart;
import org.pitest.verifier.mutants.MutatorVerifierStart;

public class GroovyFilterTest {

    InterceptorVerifier v = VerifierStart.forInterceptorFactory(new GroovyFilterFactory())
            .usingMutator(new NullMutateEverything());

    @Test
    public void doesNotFilterMutationsInJavaClasses() {
        v.forClass(CoveredBeforeExceptionTestee.class)
                .forAnyCode()
                .mutantsAreGenerated()
                .noMutantsAreFiltered()
                .verify();
    }

    @Test
    public void filtersMutantsInGroovyClasses() {
        v.usingResourceFolder("groovy")
                .forClass("SomeGroovyCode")
                .forAnyCode()
                .mutantsAreGenerated()
                .allMutantsAreFiltered()
                .verify();
    }

    @Test
    public void filtersMutantsInGroovyClosures() {
        v.usingResourceFolder("groovy")
                .forClass("SomeGroovyCode$_mapToString_closure2")
                .forAnyCode()
                .mutantsAreGenerated()
                .allMutantsAreFiltered()
                .verify();
    }
}
