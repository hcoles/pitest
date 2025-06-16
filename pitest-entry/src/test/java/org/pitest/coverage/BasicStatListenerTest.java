package org.pitest.coverage;


import org.junit.Test;
import org.pitest.testapi.Description;

import static org.assertj.core.api.Assertions.assertThat;
import static org.pitest.coverage.CoverageMother.aBlockLocation;

public class BasicStatListenerTest {

    BasicStatListener testee = new BasicStatListener();

    @Test
    public void noMessagesWhenNoTestData() {
        assertThat(testee.messages()).isEmpty();
    }

    @Test
    public void storesSlowestTest() {
        CoverageResult cr1 = CoverageMother.aCoverageResult()
                .withExecutionTime(19)
                .build();

        Description d = new Description("foo", "bar");
        CoverageResult cr2 = CoverageMother.aCoverageResult()
                .withExecutionTime(20)
                .withTestUnitDescription(d)
                .build();

        CoverageResult cr3 = CoverageMother.aCoverageResult()
                .withExecutionTime(19)
                .build();

        testee.accept(cr1);
        testee.accept(cr2);
        testee.accept(cr3);

        assertThat(testee.messages())
                .contains("Slowest test (Description [testClass=bar, name=foo]) took 20 ms");
    }

    @Test
    public void storesLargestTest() {
        CoverageResult cr1 = CoverageMother.aCoverageResult()
                .withVisitedBlocks(aBlockLocation().build(3))
                .build();

        Description d = new Description("foo", "bar");
        CoverageResult cr2 = CoverageMother.aCoverageResult()
                .withVisitedBlocks(aBlockLocation().build(4))
                .withTestUnitDescription(d)
                .build();

        CoverageResult cr3 = CoverageMother.aCoverageResult()
                .withVisitedBlocks(aBlockLocation().build(3))
                .build();

        testee.accept(cr1);
        testee.accept(cr2);
        testee.accept(cr3);

        assertThat(testee.messages())
                .contains("Largest test (Description [testClass=bar, name=foo]) covered 4 blocks");
    }

}
