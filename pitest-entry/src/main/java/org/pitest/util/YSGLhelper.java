package org.pitest.util;

import java.util.Set;

import edu.illinois.yasgl.DirectedGraph;
import edu.illinois.yasgl.DirectedGraphBuilder;
import edu.illinois.yasgl.GraphVertexVisitor;

public class YSGLhelper {
    public static Set<String> computeReachabilityFromChangedClasses(Set<String> changed, DirectedGraph<String> graph) {
        final Set<String> reachable = graph.acceptForward(changed, new GraphVertexVisitor<String>() {
            @Override
            public void visit(String name) {
            }
        });
        return reachable;
    }

    public static Set<String> reverseReachabilityFromChangedClasses(Set<String> changed, DirectedGraph<String> graph) {
        final Set<String> reachable = graph.acceptBackward(changed, new GraphVertexVisitor<String>() {
            @Override
            public void visit(String name) {
            }
        });
        return reachable;
    }
}
