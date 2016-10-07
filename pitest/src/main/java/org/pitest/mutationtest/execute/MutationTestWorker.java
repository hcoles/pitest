/*
 * Copyright 2010 Henry Coles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest.mutationtest.execute;

import static org.pitest.util.Unchecked.translateCheckedException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassPath;
import org.pitest.functional.F3;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.mocksupport.JavassistInterceptor;
import org.pitest.testapi.TestResult;
import org.pitest.testapi.TestUnit;
import org.pitest.testapi.execute.Container;
import org.pitest.testapi.execute.ExitingResultCollector;
import org.pitest.testapi.execute.MultipleTestGroup;
import org.pitest.testapi.execute.Pitest;
import org.pitest.testapi.execute.containers.ConcreteResultCollector;
import org.pitest.testapi.execute.containers.UnContainer;
import org.pitest.util.IsolationUtils;
import org.pitest.util.Log;

public class MutationTestWorker {

    private static final Logger LOG = Log.getLogger();

    // micro optimise debug logging
    private static final boolean DEBUG = LOG.isLoggable(Level.FINE);

    private final Mutater mutater;
    private final ClassLoader loader;
    private final F3<ClassName, ClassLoader, byte[], Boolean> hotswap;

    public MutationTestWorker(final F3<ClassName, ClassLoader, byte[], Boolean> hotswap, final Mutater mutater, final ClassLoader loader) {
        this.loader = loader;
        this.mutater = mutater;
        this.hotswap = hotswap;
    }

    protected void run(final Collection<MutationDetails> range, final Reporter r, final TimeOutDecoratedTestSource testSource) throws IOException {

        for (final MutationDetails mutation : range) {
            if (DEBUG) {
                LOG.fine("Running mutation " + mutation);
            }
            final long t0 = System.currentTimeMillis();
            processMutation(r, testSource, mutation);
            if (DEBUG) {
                LOG.fine("processed mutation in " + (System.currentTimeMillis() - t0) + " ms.");
            }
        }

    }

    private void processMutation(final Reporter r, final TimeOutDecoratedTestSource testSource, final MutationDetails mutationDetails) throws IOException {

        final MutationIdentifier mutationId = mutationDetails.getId();
        final Mutant mutatedClass = this.mutater.getMutation(mutationId);

        // For the benefit of mocking frameworks such as PowerMock
        // mess with the internals of Javassist so our mutated class
        // bytes are returned
        JavassistInterceptor.setMutant(mutatedClass);

        if (DEBUG) {
            LOG.fine("mutating method " + mutatedClass.getDetails().getMethod());
        }
        final List<TestUnit> relevantTests = testSource.translateTests(mutationDetails.getTestsInOrder());

        r.describe(mutationId);

        final MutationStatusTestPair mutationDetected = handleMutation(mutationDetails, mutatedClass, relevantTests);

        LOG.info("mutationDetected status " + mutationDetected.getStatus());

        final String mutatedClassFilePath = new StringBuilder().append(System.getenv("MUTATION_DIR")).append(File.separatorChar).append(mutatedClass.getDetails().getId().getLocation().getClassName().getNameWithoutPackage().asJavaName()).append("_")
                .append(mutatedClass.getDetails().getId().getLocation().getMethodName().name()).append("_").append(mutatedClass.getDetails().getId().getFirstIndex()).append(".class").toString();
        if (DEBUG) {
            LOG.fine("writing mutated block to " + mutatedClassFilePath);
        }
        Files.write(Paths.get(mutatedClassFilePath), mutatedClass.getBytes());

        r.report(mutationId, mutationDetected);
        if (DEBUG) {
            LOG.fine("Mutation " + mutationId + " detected = " + mutationDetected);
        }
    }

    private MutationStatusTestPair handleMutation(final MutationDetails mutationId, final Mutant mutatedClass, final List<TestUnit> relevantTests) {
        MutationStatusTestPair mutationDetected;
        if ((relevantTests == null) || relevantTests.isEmpty()) {
            LOG.info("No test coverage for mutation  " + mutationId + " in " + mutatedClass.getDetails().getMethod());
            mutationDetected = new MutationStatusTestPair(0, DetectionStatus.RUN_ERROR);
        } else {
            mutationDetected = handleCoveredMutation(mutationId, mutatedClass, relevantTests);

        }
        return mutationDetected;
    }

    private MutationStatusTestPair handleCoveredMutation(final MutationDetails mutationId, final Mutant mutatedClass, final List<TestUnit> relevantTests) {
        MutationStatusTestPair mutationDetected;
        if (DEBUG) {
            LOG.fine("" + relevantTests.size() + " relevant test for " + mutatedClass.getDetails().getMethod());
        }

        final ClassLoader activeloader = pickClassLoaderForMutant(mutationId);
        final Container c = createNewContainer(activeloader);
        final long t0 = System.currentTimeMillis();
        if (this.hotswap.apply(mutationId.getClassName(), activeloader, mutatedClass.getBytes())) {
            if (DEBUG) {
                LOG.fine("replaced class with mutant in " + (System.currentTimeMillis() - t0) + " ms");
            }
            mutationDetected = doTestsDetectMutation(c, relevantTests);
        } else {
            LOG.warning("Mutation " + mutationId + " was not viable ");
            mutationDetected = new MutationStatusTestPair(0, DetectionStatus.NON_VIABLE);
        }
        return mutationDetected;
    }

    private static Container createNewContainer(final ClassLoader activeloader) {
        final Container c = new UnContainer() {
            @Override
            public List<TestResult> execute(final TestUnit group) {
                List<TestResult> results = new ArrayList<TestResult>();
                final ExitingResultCollector rc = new ExitingResultCollector(new ConcreteResultCollector(results));
                group.execute(activeloader, rc);
                return results;
            }
        };
        return c;
    }

    private ClassLoader pickClassLoaderForMutant(final MutationDetails mutant) {
        if (mutant.mayPoisonJVM()) {
            if (DEBUG) {
                LOG.fine("Creating new classloader for static initializer");
            }
            return new DefaultPITClassloader(new ClassPath(), IsolationUtils.bootClassLoader());
        } else {
            return this.loader;
        }
    }

    @Override
    public String toString() {
        return "MutationTestWorker [mutater=" + this.mutater + ", loader=" + this.loader + ", hotswap=" + this.hotswap + "]";
    }

    private MutationStatusTestPair doTestsDetectMutation(final Container c, final List<TestUnit> tests) {
        try {
            final CheckTestHasFailedResultListener listener = new CheckTestHasFailedResultListener();

            final Pitest pit = new Pitest(Collections.singletonList(listener));
            pit.run(c, createEarlyExitTestGroup(tests));

            return createStatusTestPair(listener);
        } catch (final Exception ex) {
            throw translateCheckedException(ex);
        }

    }

    private MutationStatusTestPair createStatusTestPair(final CheckTestHasFailedResultListener listener) {
        if (listener.lastFailingTest().hasSome()) {
            return new MutationStatusTestPair(listener.getNumberOfTestsRun(), listener.status(), listener.lastFailingTest().value().getQualifiedName());
        } else {
            return new MutationStatusTestPair(listener.getNumberOfTestsRun(), listener.status());
        }
    }

    private List<TestUnit> createEarlyExitTestGroup(final List<TestUnit> tests) {
        return Collections.<TestUnit> singletonList(new MultipleTestGroup(tests));
    }

}
