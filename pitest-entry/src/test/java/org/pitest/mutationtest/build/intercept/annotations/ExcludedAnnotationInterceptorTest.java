package org.pitest.mutationtest.build.intercept.annotations;

import org.junit.Test;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.engine.gregor.mutators.NullMutateEverything;
import org.pitest.verifier.interceptors.InterceptorVerifier;
import org.pitest.verifier.interceptors.VerifierStart;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;


import static org.assertj.core.api.Assertions.assertThat;

public class ExcludedAnnotationInterceptorTest {

  ExcludedAnnotationInterceptor testee = new ExcludedAnnotationInterceptor(Arrays.asList("TestGeneratedAnnotation", "AnotherTestAnnotation"));

  InterceptorVerifier v = VerifierStart.forInterceptor(testee)
          .usingMutator(new NullMutateEverything());


  @Test
  public void shouldDeclareSelfAsFilter() {
    assertThat(this.testee.type()).isEqualTo(InterceptorType.FILTER);
  }

  @Test
  public void shouldNotFilterMutationsWhenNoAnnotations() {
    v.forClass(UnAnnotated.class)
            .forAnyCode()
            .mutantsAreGenerated()
            .noMutantsAreFiltered()
            .verify();
  }

  @Test
  public void shouldFilterAllMutationsForClassesWithGeneratedAnnotation() {
    v.forClass(AnnotatedWithGenerated.class)
            .forAnyCode()
            .mutantsAreGenerated()
            .allMutantsAreFiltered()
            .verify();
  }

  @Test
  public void shouldFilterAllMutationsForClassesWithDoNoMutateAnnotation() {
    v.forClass(AnnotatedWithDoNotMutate.class)
            .forAnyCode()
            .mutantsAreGenerated()
            .allMutantsAreFiltered()
            .verify();
  }

  @Test
  public void shouldFilterMethodsWithGeneratedAnnotation() {
    v.forClass(MethodAnnotatedWithGenerated.class)
            .forMethod("foo")
            .forAnyCode()
            .mutantsAreGenerated()
            .allMutantsAreFiltered()
            .verify();

    v.forClass(MethodAnnotatedWithGenerated.class)
            .forMethod("bar")
            .forAnyCode()
            .mutantsAreGenerated()
            .noMutantsAreFiltered()
            .verify();

  }

  @Test
  public void shouldNotFilterMutationsInUnannotatedMethod() {
    v.forClass(UnannotatedMethodClass.class)
            .forAnyCode()
            .mutantsAreGenerated()
            .noMutantsAreFiltered()
            .verify();
  }

  @Test
  public void shouldFilterMutationsInAnnotatedMethod() {
    v.forClass(UnannotatedMethodClass.class)
            .forMethod("unannotatedMethod")
            .forAnyCode()
            .mutantsAreGenerated()
            .noMutantsAreFiltered()
            .verify();
  }

  @Test
  public void shouldNotFilterMutationsInLambdaWithinUnannotatedMethod() {
    v.forClass(LambdaInUnannotatedMethodClass.class)
            .forAnyCode()
            .mutantsAreGenerated()
            .noMutantsAreFiltered()
            .verify();
  }

  @Test
  public void shouldFilterMutationsInLambdaWithinAnnotatedMethod() {
    v.forClass(LambdaInAnnotatedMethodClass.class)
            .forMutantsMatching( m -> !m.getMethod().equals("<init>"))
            .mutantsAreGenerated()
            .allMutantsAreFiltered()
            .verify();
  }

  @Test
  public void shouldHandleOverloadedMethodsWithLambdas() {
    v.forClass(OverloadedMethods.class)
            .forMethod("foo", "(Ljava/lang/String;)V")
            .forAnyCode()
            .mutantsAreGenerated()
            .allMutantsAreFiltered()
            .verify();

    v.forClass(OverloadedMethods.class)
            .forMethod("lambda$foo$1", "()V")
            .forAnyCode()
            .mutantsAreGenerated()
            .allMutantsAreFiltered()
            .verify();

    v.forClass(OverloadedMethods.class)
            .forMethod("lambda$foo$0", "()V")
            .forAnyCode()
            .mutantsAreGenerated()
            .noMutantsAreFiltered()
            .verify();

    v.forClass(OverloadedMethods.class)
            .forMethod("bar")
            .forAnyCode()
            .mutantsAreGenerated()
            .noMutantsAreFiltered()
            .verify();

  }

  @Test
  public void shouldNotFilterMutationsInNestedLambdaWithinUnannotatedOverloadedMethod() {
    v.forClass(NestedLambdaInOverloadedMethods.class)
            .forMethod("baz", "(Ljava/lang/String;)V")
            .forAnyCode()
            .mutantsAreGenerated()
            .allMutantsAreFiltered()
            .verify();

    v.forClass(NestedLambdaInOverloadedMethods.class)
            .forMethod("lambda$baz$3")
            .forAnyCode()
            .mutantsAreGenerated()
            .allMutantsAreFiltered()
            .verify();

    v.forClass(NestedLambdaInOverloadedMethods.class)
            .forMethod("lambda$baz$2")
            .forAnyCode()
            //.mutantsAreGenerated() no check as java8 produces different bytecode
            .allMutantsAreFiltered()
            .verify();

    v.forClass(NestedLambdaInOverloadedMethods.class)
            .forMethod("lambda$baz$0")
            .forAnyCode()
            //.mutantsAreGenerated() no check as java8 produces different bytecode
            .noMutantsAreFiltered()
            .verify();

    v.forClass(NestedLambdaInOverloadedMethods.class)
            .forMethod("lambda$baz$1")
            .forAnyCode()
            .mutantsAreGenerated()
            .noMutantsAreFiltered()
            .verify();

  }

  @Test
  public void shouldFilterMutationsInNestedLambdaWithinAnnotatedOverloadedMethod() {
    v.forClass(NestedLambdaInOverloadedMethods.class)
            .forMutantsMatching(mutation -> mutation.getId().getLocation().getMethodDesc().equals("(Ljava/lang/String;)V"))
            .mutantsAreGenerated()
            .allMutantsAreFiltered()
            .verify();
  }


}

class UnAnnotated {

}

@TestGeneratedAnnotation
class AnnotatedWithGenerated {
  public void foo() {
    System.out.println("don't mutate me");
  }
}

@AnotherTestAnnotation
class AnnotatedWithDoNotMutate {
  public void foo() {
    System.out.println("don't mutate me");
  }
}

class MethodAnnotatedWithGenerated {
  @TestGeneratedAnnotation
  public void foo() {
    System.out.println("don't mutate me");
  }

  public void bar() {
    System.out.println("mutate me");
  }
}

@Retention(value=RetentionPolicy.RUNTIME)
@interface TestGeneratedAnnotation {

}

@Retention(value=RetentionPolicy.CLASS)
@interface AnotherTestAnnotation {

}

class UnannotatedMethodClass {
  public void unannotatedMethod() {
    System.out.println("This method is not annotated.");
  }
}

class AnnotatedMethodClass {
  @TestGeneratedAnnotation
  public void annotatedMethod() {
    System.out.println("This method is annotated.");
  }
}

class LambdaInUnannotatedMethodClass {
  public void methodWithLambda() {
    Runnable runnable = () -> System.out.println("Lambda inside unannotated method.");
  }
}

class LambdaInAnnotatedMethodClass {
  @TestGeneratedAnnotation
  public void methodWithLambda() {
    Runnable runnable = () -> System.out.println("Lambda inside annotated method.");
  }
}

class OverloadedMethods {
  public void foo(int x) {
    System.out.println("mutate me");
    Runnable r = () -> System.out.println("Lambda in unannotated overloaded method with int");
  }

  @TestGeneratedAnnotation
  public void foo(String x) {
    System.out.println("don't mutate me");
    Runnable r = () -> System.out.println("Lambda in annotated overloaded method with String");
  }

  public void bar() {
    System.out.println("mutate me");
  }
}

class NestedLambdaInOverloadedMethods {
  public void baz(int x) {
    System.out.println("mutate me");
    Runnable outerLambda = () -> {
      Runnable innerLambda = () -> System.out.println("Nested lambda in unannotated overloaded method with int");
    };
  }

  @TestGeneratedAnnotation
  public void baz(String x) {
    System.out.println("don't mutate me");
    Runnable outerLambda = () -> {
      Runnable innerLambda = () -> System.out.println("Nested lambda in annotated overloaded method with String");
    };
  }
}
