package org.pitest.kotlin.test

import org.junit.Assert
import org.junit.Test
import org.pitest.mutationtest.DetectionStatus
import org.pitest.mutationtest.MetaDataExtractor
import org.pitest.mutationtest.MutationResultListener
import org.pitest.mutationtest.TestMutationTesting
import org.pitest.mutationtest.engine.gregor.config.Mutator
import org.pitest.mutationtest.execute.MutationAnalysisExecutor
import org.pitest.simpletest.ConfigurationForTesting
import org.pitest.simpletest.TestAnnotationForTesting

class KotlinTest {
    private val config = ConfigurationForTesting()

    private var metaDataExtractor = MetaDataExtractor()
    private var mae = MutationAnalysisExecutor(1,
            listOf<MutationResultListener>(this.metaDataExtractor))



    @Test
    fun shouldIgnoreKotlinIntrinsics() {
        TestMutationTesting.`run`(FullyCovereredClass::class.java, FullyCovereredClassTest::class.java,
                Mutator.defaults(), config, mae)
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
