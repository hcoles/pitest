package org.pitest.coverage;

import org.pitest.testapi.Description;

import java.util.Collections;
import java.util.List;

public class BasicStatListener implements TestStatListener {

    // arbitrary limit to gauge number of slow tests
    private static final int SLOW_LIMIT = 2000;

    private int slowTestCount = 0;
    private TestStat slowestTest = new TestStat(0, null);
    private TestStat largestTest = new TestStat(0, null);

    @Override
    public void accept(CoverageResult cr) {
        if (cr.getExecutionTime() > SLOW_LIMIT) {
            slowTestCount = slowTestCount + 1;
        }

        if (cr.getExecutionTime() >= slowestTest.stat()) {
            slowestTest = new TestStat(cr.getExecutionTime(), cr.getTestUnitDescription());
        }

        if (cr.getNumberOfCoveredBlocks() >= largestTest.stat()) {
            largestTest = new TestStat(cr.getNumberOfCoveredBlocks(), cr.getTestUnitDescription());
        }
    }

    @Override
    public List<String> messages() {
        if (slowestTest.stat() == 0) {
            return Collections.emptyList();
        }
        return List.of(
                slowTestCount + " tests took longer than " + SLOW_LIMIT + " ms",
                "Slowest test (" + slowestTest.test().getName() + ") took " + slowestTest.stat() + " ms",
                "Largest test (" + largestTest.test().getName() + ") covered " + largestTest.stat() + " blocks"
                );
    }

    @Override
    public void end() {

    }
}

class TestStat {
    private final int stat;
    private final Description test;

    TestStat(int stat, Description test) {
        this.stat = stat;
        this.test = test;
    }

    public int stat() {
        return stat;
    }
    public Description test() {
        return test;
    }
}