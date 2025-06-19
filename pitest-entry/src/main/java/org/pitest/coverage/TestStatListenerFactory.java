package org.pitest.coverage;

import org.pitest.plugin.ProvidesFeature;
import org.pitest.plugin.ToolClasspathPlugin;
import org.pitest.util.ResultOutputStrategy;

public interface TestStatListenerFactory extends ToolClasspathPlugin, ProvidesFeature {

    TestStatListener createTestListener(ResultOutputStrategy outputStrategy);
}
