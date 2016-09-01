package org.pitest.kotlin.test

import org.junit.Assert
import org.junit.Test
import org.pitest.classinfo.ClassInfo
import org.pitest.classpath.ClassloaderByteArraySource
import org.pitest.classpath.CodeSource
import org.pitest.classpath.PathFilter
import org.pitest.classpath.ProjectClassPaths
import org.pitest.coverage.execute.CoverageOptions
import org.pitest.coverage.execute.DefaultCoverageGenerator
import org.pitest.coverage.export.NullCoverageExporter
import org.pitest.functional.FCollection
import org.pitest.functional.predicate.False
import org.pitest.functional.prelude.Prelude
import org.pitest.mutationtest.*
import org.pitest.mutationtest.build.*
import org.pitest.mutationtest.config.DefaultDependencyPathPredicate
import org.pitest.mutationtest.config.ReportOptions
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory
import org.pitest.mutationtest.engine.gregor.config.GregorEngineFactory
import org.pitest.mutationtest.engine.gregor.config.Mutator
import org.pitest.mutationtest.execute.MutationAnalysisExecutor
import org.pitest.mutationtest.filter.UnfilteredMutationFilter
import org.pitest.mutationtest.tooling.JarCreatingJarFinder
import org.pitest.process.DefaultJavaExecutableLocator
import org.pitest.process.JavaAgent
import org.pitest.process.LaunchOptions
import org.pitest.simpletest.ConfigurationForTesting
import org.pitest.simpletest.TestAnnotationForTesting
import org.pitest.util.Functions
import org.pitest.util.IsolationUtils
import org.pitest.util.Timings
import java.util.*

class KotlinTest {
    private val config = ConfigurationForTesting()

    private var metaDataExtractor = MetaDataExtractor()
    private var mae = MutationAnalysisExecutor(1,
            listOf<MutationResultListener>(this.metaDataExtractor))


    private fun runIt(clazz: Class<*>, test: Class<*>,
                      mutators: Collection<MethodMutatorFactory>) {

        val data = ReportOptions()

        val tests = setOf(Prelude.isEqualTo(test.name))
        data.targetTests = tests
        data.dependencyAnalysisMaxDistance = -1

        val mutees = setOf(Functions.startsWith(clazz.name))
        data.targetClasses = mutees

        data.timeoutConstant = PercentAndConstantTimeoutStrategy.DEFAULT_CONSTANT
        data.timeoutFactor = PercentAndConstantTimeoutStrategy.DEFAULT_FACTOR

        val agent = JarCreatingJarFinder()

        try {
            createEngineAndRun(data, agent, mutators)
        } finally {
            agent.close()
        }
    }

    private fun createEngineAndRun(data: ReportOptions,
                                   agent: JavaAgent,
                                   mutators: Collection<MethodMutatorFactory>) {

        // data.setConfiguration(this.config);
        val coverageOptions = createCoverageOptions(data)

        val launchOptions = LaunchOptions(agent,
                DefaultJavaExecutableLocator(), data.jvmArgs,
                HashMap<String, String>())

        val pf = PathFilter(
                Prelude.not(DefaultDependencyPathPredicate()),
                Prelude.not(DefaultDependencyPathPredicate()))
        val cps = ProjectClassPaths(data.classPath,
                data.createClassesFilter(), pf)

        val timings = Timings()
        val code = CodeSource(cps, coverageOptions.getPitConfig().testClassIdentifier())

        val coverageGenerator = DefaultCoverageGenerator(
                null, coverageOptions, launchOptions, code, NullCoverageExporter(),
                timings, false)

        val coverageData = coverageGenerator.calculateCoverage()

        val codeClasses = FCollection.map(code.code,
                ClassInfo.toClassName())

        val engine = GregorEngineFactory().createEngineWithMutators(false, False.instance<String>(),
                emptyList<String>(), mutators, true)

        val mutationConfig = MutationConfig(engine,
                launchOptions)

        val bas = ClassloaderByteArraySource(
                IsolationUtils.getContextClassLoader())
        val source = MutationSource(mutationConfig,
                UnfilteredMutationFilter.INSTANCE, DefaultTestPrioritiser(
                coverageData), bas)

        val wf = WorkerFactory(null,
                coverageOptions.getPitConfig(), mutationConfig,
                PercentAndConstantTimeoutStrategy(data.timeoutFactor,
                        data.timeoutConstant), data.isVerbose, data.classPath.localClassPath)

        val builder = MutationTestBuilder(wf,
                NullAnalyser(), source, DefaultGrouper(0))

        val tus = builder.createMutationTestUnits(codeClasses)

        this.mae.run(tus)
    }

    private fun createCoverageOptions(data: ReportOptions): CoverageOptions {
        return CoverageOptions(data.targetClassesFilter, this.config,
                data.isVerbose, data.dependencyAnalysisMaxDistance)
    }

    @Test
    fun shouldDetectedMixOfSurvivingAndKilledMutations() {
        runIt(FullyCovereredClass::class.java, FullyCovereredClassTest::class.java,
                Mutator.defaults())
        Assert.assertFalse(metaDataExtractor.detectionStatus.contains(DetectionStatus.NO_COVERAGE))
    }

    class FullyCovereredClass {
        fun notNullReturnsInputValue(s: String): Any {
            return s
        }
    }

    class FullyCovereredClassTest {
        @TestAnnotationForTesting fun shouldReturn3() {
            Assert.assertEquals(FullyCovereredClass().notNullReturnsInputValue("blubby"), "blubby")
        }
    }

}
