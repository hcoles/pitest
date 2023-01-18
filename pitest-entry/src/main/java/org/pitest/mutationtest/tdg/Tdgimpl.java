package org.pitest.mutationtest.tdg;

import edu.illinois.yasgl.DirectedGraph;
import org.pitest.classinfo.ClassName;
import java.util.Collection;
import org.pitest.coverage.TestInfo;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import org.pitest.classpath.ProjectClassPaths;
import org.pitest.util.Log;
import java.io.File;
import java.util.Arrays;
import org.pitest.util.AgentLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.io.StringWriter;
import edu.illinois.yasgl.DirectedGraphBuilder;
import org.pitest.util.YSGLhelper;
import java.util.stream.Collectors;
import org.pitest.classinfo.ClassName;
import java.lang.reflect.Method;
import java.util.stream.Stream;
import org.junit.Test;
import java.util.Optional;
public class Tdgimpl implements Tdg{
    private ProjectClassPaths classPath;
    String pathToUse;
    String targetClassPath;
    String testClassPath;
    private Map<String, Set<String>> tc;
    private Map<String, Set<String>> target2Tests;
    private Map<String, Set<String>> classMethodNames;
    Map<String, Map<String, Integer> > reachableWithWeigh = new HashMap<>();
    public Tdgimpl(ProjectClassPaths cps,  Map<String, Set<String>> classMethodNames) {
        if (classMethodNames == null) {
            System.out.println("init classMethodNames null");
        }
        this.classMethodNames = classMethodNames;
        this.classPath = cps;
        pathToUse =  cps.getClassPath().getComponent(cps.getPathFilter().getTestFilter()).getLocalClassPath();
        targetClassPath = cps.getClassPath().getComponent(cps.getPathFilter().getCodeFilter()).getLocalClassPath();
        List<String> paths = Arrays.asList(pathToUse.split(File.pathSeparator));
        if (paths.size() != 2) {
            Log.getLogger().warning("Tdgimpl paths.size() != 2");
        }
        if (paths.get(0) != targetClassPath) testClassPath = paths.get(0);
        else testClassPath = paths.get(1);
        Log.getLogger().info("targetClassPath : " + targetClassPath);
        Log.getLogger().info("testClassPath : " + testClassPath);
    }
    public ProjectClassPaths getProjectClassPaths() {
        return this.classPath;
    }
    private DirectedGraphBuilder<String> getBuilderFromDeps(Map<String, Set<String>> deps) {
        DirectedGraphBuilder<String> builder = new DirectedGraphBuilder<>();
        for (String key : deps.keySet()) {
            for (String dep : deps.get(key)) {
                builder.addEdge(key, dep);
            }
        }
        return builder;
    }
    public void init() {
        this.tc = this.getClosure();
        if (this.tc == null) {
            Log.getLogger().warning("getClosure failed");    
        }
        // Log.getLogger().info("this.classPath.test() : " + this.classPath.test());
        Collection<ClassName> targets = this.classPath.code();
        Collection<String> tests = this.classPath.test().stream().filter(s -> !targets.contains(s)).map(ClassName::toString).collect(Collectors.toList());
        // Log.getLogger().info("this.classPath.tests : " + tests);
        Map<String, Set<String>> target2Tests = new HashMap<>();
        for (String key : this.tc.keySet()) {
            target2Tests.put(key,this.tc.get(key).stream().filter(s -> tests.contains(s)).collect(Collectors.toSet()));
        }
        this.target2Tests = target2Tests;
        // Log.getLogger().info("after filtered  this.target2Tests  : " + target2Tests);
        // System.out.println("reachableWithWeigh" + reachableWithWeigh);
        // Log.getLogger().info("no no no no  dist");
        Log.getLogger().info("test order with dist");
    }

    @Override
    public Collection<TestInfo> getTests(ClassName name) {
        // System.out.println("findTests for " + name );
        if (target2Tests == null || classMethodNames == null) {
            // System.out.println("target2Tests or classMethodNames null, er!!");
            return new ArrayList<TestInfo>();
        }
        // System.out.println("target2Tests target2Tests target2Tests : " + target2Tests);
        // System.out.println("=========================================================== : ");
        // System.out.println("classMethodNames classMethodNames classMethodNames : " + classMethodNames);
        List<TestInfo> tf = new ArrayList<>();
        for (String testClass : target2Tests.get(name.toString())) {
            List<TestInfo> res = new ArrayList<>();
            if (!classMethodNames.containsKey(testClass)) continue;
            for (String methodNames : classMethodNames.get(testClass) ) {
                
                // res.add(this.createFromMethodName(methodNames, testClass));
                
                res.add(this.createFromMethodName(methodNames, testClass,name.toString()));// 开启距离排序
            }
            tf.addAll(res);
        }
        return tf;
    }

    public Collection<TestInfo> getAllTests() {
        List<TestInfo> tf = new ArrayList<>();
        for (String key : this.classMethodNames.keySet()) {
            for (String methodName : this.classMethodNames.get(key)) {
                tf.add(createFromMethodName(methodName, key));
            }
        }
        return tf;
    }
    private TestInfo createFromMethodName(String methodName, String testClass,String testee) {
        return new TestInfo(testClass, testClass+"."+methodName, 1, Optional.<ClassName> empty(), 1, reachableWithWeigh.get(testee).get(testClass));
    }
    private TestInfo createFromMethodName(String methodName, String testClass) {
        return new TestInfo(testClass, testClass+"."+methodName, 1, Optional.<ClassName> empty(), 1);
    }

    @Override
    public DirectedGraph getTdg() {
        DirectedGraphBuilder<String> builder = getBuilderFromDeps(this.getDepsMap());
        return builder.build();
    }

    @Override
    public Map<String, Set<String>> getClosure() {
        return this.getTransitiveClosurePerClass(this.getTdg(),classPath.code().stream().map(ClassName::toString).collect(Collectors.toList()));
    }
    
    private Map<String, Set<String>> getTransitiveClosurePerClass(DirectedGraph<String> tcGraph,
                                                                  List<String> classesToAnalyze) {
        Map<String, Set<String>> tcPerClass = new HashMap<>();
        for (String clazz : classesToAnalyze) {
            Set<String> deps = YSGLhelper.reverseReachabilityFromChangedClassesWithDist(
                clazz, tcGraph,reachableWithWeigh);
            deps.add(clazz);
            tcPerClass.put(clazz, deps);
        }
        return tcPerClass;
    }

    public Map<String, Set<String>> getDepsMap() {
        List<String> args = new ArrayList<>(Arrays.asList("-v"));
        args.addAll(Arrays.asList("-filter", "java.*|sun.*"));
        args.addAll(Arrays.asList("-cp", pathToUse));
        args.add(testClassPath);
        args.add(targetClassPath);
        // Log.getLogger().info("jdeps args : " + args);
        return getDepsFromJdepsOutput(AgentLoader.loadAndRunJdeps(args));
    }

    public static Map<String, Set<String>> getDepsFromJdepsOutput(StringWriter jdepsOutput) {
        Map<String, Set<String>> deps = new HashMap<>();
        List<String> lines = Arrays.asList(jdepsOutput.toString().split(System.lineSeparator()));
        for (String line : lines) {
            String[] parts = line.split("->");
            String left = parts[0].trim();
            if (left.startsWith("classes") || left.startsWith("test-classes") || left.endsWith(".jar")) {
                continue;
            }
            String right = parts[1].trim().split(" ")[0];
            if (deps.keySet().contains(left)) {
                deps.get(left).add(right);
            } else {
                deps.put(left, new HashSet<>(Arrays.asList(right)));
            }
        }
        return deps;
    }
}
