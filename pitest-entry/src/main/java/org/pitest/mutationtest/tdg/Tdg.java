package org.pitest.mutationtest.tdg;

import edu.illinois.yasgl.DirectedGraph;
import org.pitest.classinfo.ClassName;
import java.util.Collection;
import org.pitest.coverage.TestInfo;
import java.util.Map;
import java.util.Set;

public interface Tdg {
    Collection<TestInfo> getTests(ClassName name);
    DirectedGraph getTdg();
    Map<String, Set<String>> getClosure();
}
