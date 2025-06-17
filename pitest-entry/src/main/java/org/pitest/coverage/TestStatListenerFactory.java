package org.pitest.coverage;

import org.pitest.plugin.ToolClasspathPlugin;
import org.pitest.util.ResultOutputStrategy;

public interface TestStatListenerFactory extends ToolClasspathPlugin {

    TestStatListener createTestListener(ResultOutputStrategy outputStrategy);
}
