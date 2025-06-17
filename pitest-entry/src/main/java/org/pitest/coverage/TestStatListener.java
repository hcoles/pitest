package org.pitest.coverage;

import java.util.List;

public interface TestStatListener {
    void accept(CoverageResult cr);

    List<String> messages();

    void end();
}
