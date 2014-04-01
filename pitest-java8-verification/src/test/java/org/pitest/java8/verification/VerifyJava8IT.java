package org.pitest.java8.verification;

import java.io.IOException;

import org.pitest.classpath.ClassPathRoot;
import org.pitest.classpath.CodeSource;
import org.pitest.classpath.PathFilter;
import org.pitest.classpath.ProjectClassPaths;
import org.pitest.coverage.CoverageGenerator;
import org.pitest.coverage.execute.CoverageOptions;
import org.pitest.coverage.execute.DefaultCoverageGenerator;
import org.pitest.coverage.export.NullCoverageExporter;
import org.pitest.functional.predicate.True;
import org.pitest.junit.JUnitCompatibleConfiguration;
import static org.pitest.mutationtest.DetectionStatus.KILLED;

import static org.junit.Assert.*;

import org.junit.Test;

import com.example.java8.AnonymousClassTest;
import com.example.java8.Java8ClassTest;
import com.example.java8.Java8InterfaceTest;
import com.example.java8.Java8LambdaExpressionTest;

import org.pitest.mutationtest.HistoryStore;
import org.pitest.mutationtest.ReportTestBase;
import org.pitest.mutationtest.config.SettingsFactory;
import org.pitest.mutationtest.engine.gregor.config.GregorEngineFactory;
import org.pitest.mutationtest.engine.gregor.config.Mutator;
import org.pitest.mutationtest.incremental.NullHistoryStore;
import org.pitest.mutationtest.tooling.JarCreatingJarFinder;
import org.pitest.mutationtest.tooling.MutationCoverage;
import org.pitest.mutationtest.tooling.MutationStrategies;
import org.pitest.process.DefaultJavaExecutableLocator;
import org.pitest.process.JavaAgent;
import org.pitest.process.LaunchOptions;
import org.pitest.testapi.Configuration;
import org.pitest.util.Timings;
import org.pitest.util.Unchecked;


public class VerifyJava8IT extends ReportTestBase { 

  // Java 8 support

  /**
   * @author iirekm@gmail.com
   */
  @Test
  public void worksWithJava8Bytecode() {
      this.data.setTargetTests(predicateFor(Java8ClassTest.class));
      this.data.setTargetClasses(predicateFor("com.example.java8.Java8Class*"));
      setMutators("INCREMENTS");
      createAndRun();
      verifyResults(KILLED, KILLED);
  }

  /**
   * @author iirekm@gmail.com
   */
  @Test
  public void worksWithJava8DefaultInterfaceMethods() {
      this.data.setTargetTests(predicateFor(Java8InterfaceTest.class));
      this.data.setTargetClasses(predicateFor("com.example.java8.Java8Interface*"));
      setMutators("INCREMENTS");
      createAndRun();
      verifyResults(KILLED, KILLED);
  }

  /**
   * @author iirekm@gmail.com
   *
   * Initial step for Java 8 lambda expressions: check if pure anonymous classes work.
   */
  @Test
  public void worksWithAnonymousClasses() {
      this.data.setTargetTests(predicateFor(AnonymousClassTest.class));
      this.data.setTargetClasses(predicateFor("com.example.java8.AnonymousClass*"));
      setMutators("INCREMENTS");
      createAndRun();
      verifyResults(KILLED, KILLED);
  }

  /**
   * @author iirekm@gmail.com
   */
  @Test
  public void worksWithJava8LambdaExpressions() {
      this.data.setTargetTests(predicateFor(Java8LambdaExpressionTest.class));
      this.data.setTargetClasses(predicateFor("com.example.java8.Java8LambdaExpression*"));
      setMutators("INCREMENTS");
      createAndRun();
      verifyResults(KILLED, KILLED);
  }
  
}
