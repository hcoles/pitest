package org.pitest.util;

import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import edu.illinois.yasgl.DirectedGraph;
import edu.illinois.yasgl.DirectedGraphBuilder;
import edu.illinois.yasgl.GraphVertexVisitor;
import edu.illinois.yasgl.MyGraphVertexVisitor;
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
    public static Set<String> reverseReachabilityFromChangedClassesWithDist(String changed, 
                    DirectedGraph<String> graph, Map<String, Map<String, Integer> > reachableWithWeigh) {
        // Set<String> reachableWithWeigh = new HashSet<>();
        //  = new HashMap<>();
        Map<String, Integer> weight = new HashMap<>();
        weight.clear();
        final Set<String> reachable = graph.MyacceptBackward(changed, new MyGraphVertexVisitor<String>() {
            
            // int deep = 0;
            @Override
            public void visit(String name, int deep) {
                weight.put(name, deep);
            }

        });
        reachableWithWeigh.put(changed, weight);
        // System.out.println(reachableWithWeigh);
        return reachable;
    }
}
