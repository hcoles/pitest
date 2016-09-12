package org.pitest.kotlin.test

import org.junit.Assert
import org.junit.Test
import org.pitest.mutationtest.*
import org.pitest.mutationtest.engine.gregor.config.Mutator
import org.pitest.mutationtest.execute.MutationAnalysisExecutor
import org.pitest.simpletest.ConfigurationForTesting
import org.pitest.simpletest.TestAnnotationForTesting

class KotlinTest {
    private val config = ConfigurationForTesting()

    private var metaDataExtractor = MyMetaDataExtractor()
    private var mae = MutationAnalysisExecutor(1,
            listOf<MutationResultListener>(this.metaDataExtractor))



    @Test
    fun shouldIgnoreKotlinIntrinsics() {
        TestMutationTesting.`run`(FullyCovereredClass::class.java, FullyCovereredClassTest::class.java,
                Mutator.defaults(), config, mae)
        metaDataExtractor.results.forEach { if (it.status == DetectionStatus.NO_COVERAGE || it.status == DetectionStatus.SURVIVED) Assert.fail(it.details.toString()) }
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

class MyMetaDataExtractor : MutationResultListener{
    override fun runStart() {
    }

    val results = mutableListOf<MutationResult>()

    override fun handleMutationResult(results: ClassMutationResults?) {
        this.results.addAll(results!!.mutations)
    }

    override fun runEnd() {
    }

}
