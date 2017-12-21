package org.pitest.controller;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassPath;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.coverage.TestInfo;
import org.pitest.functional.Option;
import org.pitest.functional.predicate.False;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.config.TestPluginArguments;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.engine.gregor.config.GregorEngineFactory;
import org.pitest.mutationtest.tooling.JarCreatingJarFinder;
import org.pitest.process.LaunchOptions;

import com.example.FullyCoveredTestee;
import com.example.FullyCoveredTesteeTest;

public class ControllerTest {

  @Test
  public void test() {
    
    GregorEngineFactory eng = new GregorEngineFactory();
    MutationEngine engine = eng.createEngine(False.<String>instance(), null);
    
    Mutater m = engine.createMutator(ClassloaderByteArraySource.fromContext());
    
    Collection<MutationDetails> toDo = m.findMutations(ClassName.fromClass(FullyCoveredTestee.class));
    TestInfo one = new TestInfo(FullyCoveredTesteeTest.class.getName(), "com.example.FullyCoveredTesteeTest.testCoverMe(com.example.FullyCoveredTesteeTest)", 1, Option.<ClassName>none(), 2);
    TestInfo two = new TestInfo(FullyCoveredTesteeTest.class.getName(), "com.example.FullyCoveredTesteeTest.testCoverMe(com.example.FullyCoveredTesteeTest)", 1, Option.<ClassName>none(), 2);      
    for (MutationDetails i : toDo ) {
      i.addTestsInOrder(Arrays.asList(one,two));
    }
    
    ClassPath cp = new ClassPath();
    File baseDir = new File(".");
    TestPluginArguments tp = TestPluginArguments.defaults();
    final JarCreatingJarFinder agent = new JarCreatingJarFinder();
    LaunchOptions launch = new LaunchOptions(agent);
    
    List<String> excludedMethods = Collections.emptyList();
    List<String> mutators = Collections.singletonList("STRONGER");
    
    new Controller(cp.getLocalClassPath(), baseDir, tp, "gregor", excludedMethods, mutators, true, launch).process(toDo, sysOutListener());
  }
  
  private static ResultListener sysOutListener() {
    return new ResultListener() {

      @Override
      public void report(MutationResult r) {
       System.out.println(r);
        
      }
    };
  }

}
