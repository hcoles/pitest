package org.pitest.coverage;

import java.util.List;

public class NoTestStats implements TestStatListener {
    @Override
    public void accept(CoverageResult cr) {
    }

    @Override
    public List<String> messages() {
        return List.of();
    }

    @Override
    public void end() {

    }
}
