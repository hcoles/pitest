package org.pitest.java8.verification;

import static org.pitest.mutationtest.DetectionStatus.KILLED;
import static java.util.Arrays.asList;

import org.junit.Test;
import org.pitest.mutationtest.ReportTestBase;

import com.example.java8.AnonymousClassTest;
import com.example.java8.Java8ClassTest;
import com.example.java8.Java8InterfaceTest;
import com.example.java8.Java8LambdaExpressionTest;


public class VerifyJava8IT extends ReportTestBase { 

  /**
   * @author iirekm@gmail.com
   */
  @Test
  public void worksWithJava8Bytecode() {
      this.data.setTargetTests(predicateFor(Java8ClassTest.class));
      this.data.setTargetClasses(asList("com.example.java8.Java8Class*"));
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
      this.data.setTargetClasses(asList("com.example.java8.Java8Interface*"));
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
      this.data.setTargetClasses(asList("com.example.java8.AnonymousClass*"));
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
      this.data.setTargetClasses(asList("com.example.java8.Java8LambdaExpression*"));
      setMutators("INCREMENTS");
      createAndRun();
      verifyResults(KILLED, KILLED);
  }
  
}
