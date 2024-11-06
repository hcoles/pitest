package org.pitest.mutationtest.build.intercept.annotations;

import org.junit.Before;
import org.junit.Test;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.GregorMutater;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.pitest.mutationtest.engine.MutationDetailsMother.aMutationDetail;

public class ExcludedAnnotationInterceptorTest {

  ExcludedAnnotationInterceptor testee = new ExcludedAnnotationInterceptor(Arrays.asList("TestGeneratedAnnotation", "AnotherTestAnnotation"));
  Mutater mutator;

  @Before
  public void setUp() {
    final ClassloaderByteArraySource source = ClassloaderByteArraySource.fromContext();
    final Collection<MethodMutatorFactory> mutators = Collections.singleton((MethodMutatorFactory)VoidMethodCallMutator.VOID_METHOD_CALLS);
    this.mutator = new GregorMutater(source, m -> true, mutators);
  }


  @Test
  public void shouldDeclareSelfAsFilter() {
    assertThat(this.testee.type()).isEqualTo(InterceptorType.FILTER);
  }

  @Test
  public void shouldNotFilterMutationsWhenNoAnnotations() {
    final Collection<MutationDetails> input = someMutations();
    final Collection<MutationDetails> actual = runWithTestee(input, UnAnnotated.class);
    assertThat(actual).containsExactlyElementsOf(input);
  }

  @Test
  public void shouldFilterAllMutationsForClassesWithGeneratedAnnotation() {
    final Collection<MutationDetails> actual = runWithTestee(someMutations(), AnnotatedWithGenerated.class);
    assertThat(actual).isEmpty();
  }

  @Test
  public void shouldFilterAllMutationsForClassesWithDoNoMutateAnnotation() {
    final Collection<MutationDetails> actual = runWithTestee(someMutations(), AnnotatedWithDoNotMutate.class);
    assertThat(actual).isEmpty();
  }

  @Test
  public void shouldFilterMethodsWithGeneratedAnnotation() {
    final List<MutationDetails> mutations = this.mutator.findMutations(ClassName.fromClass(MethodAnnotatedWithGenerated.class));
    final Collection<MutationDetails> actual = runWithTestee(mutations, MethodAnnotatedWithGenerated.class);
    assertThat(actual).hasSize(1);
    assertThat(actual.iterator().next().getId().getLocation().getMethodName()).isEqualTo("bar");
  }

  @Test
  public void shouldFilterMethodsWithGeneratedAnnotationAndLambdasInside() {
    final List<MutationDetails> mutations = this.mutator.findMutations(ClassName.fromClass(ClassAnnotatedWithGeneratedWithLambdas.class));
    final Collection<MutationDetails> actual = runWithTestee(mutations, ClassAnnotatedWithGeneratedWithLambdas.class);
    assertThat(actual).hasSize(3);

    for (MutationDetails mutationDetails : actual) {
       assertThat(mutationDetails.getId().getLocation().getMethodName()).isIn("barWithLambdas", "lambda$barWithLambdas$2", "lambda$barWithLambdas$3");
    }
  }

  @Test
  public void shouldHandleOverloadedMethods() {
    final List<MutationDetails> mutations = this.mutator.findMutations(ClassName.fromClass(OverloadedMethods.class));
    final Collection<MutationDetails> actual = runWithTestee(mutations, OverloadedMethods.class);
    // Assume only one overloaded version is annotated
    assertThat(actual).hasSize(2); // Assuming three methods: two overloaded (one annotated) and one regular
  }

  private Collection<MutationDetails> runWithTestee(
      Collection<MutationDetails> input, Class<?> clazz) {
    this.testee.begin(treeFor(clazz));
    return this.testee.intercept(input, this.mutator);
  }

  private Collection<MutationDetails> someMutations() {
    return aMutationDetail().build(2);
  }

  ClassTree treeFor(Class<?> clazz) {
    final ClassloaderByteArraySource source = ClassloaderByteArraySource.fromContext();
    return ClassTree.fromBytes(source.getBytes(clazz.getName()).get());
  }

}

class UnAnnotated {

}

@TestGeneratedAnnotation
class AnnotatedWithGenerated {

}

@AnotherTestAnnotation
class AnnotatedWithDoNotMutate {

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

class OverloadedMethods {
  public void foo(int x) {
    System.out.println("mutate me");
  }

  @TestGeneratedAnnotation
  public void foo(String x) {
    System.out.println("don't mutate me");
  }

  public void bar() {
    System.out.println("mutate me");
  }
}


class ClassAnnotatedWithGeneratedWithLambdas {

  @TestGeneratedAnnotation
  public void fooWithLambdas() {
    System.out.println("don't mutate me");

    Runnable runnable = () -> {
      System.out.println("don't mutate me also in lambdas");

        Runnable anotherOne = () -> {
          System.out.println("don't mutate me also recursive lambdas");
        };
    };
  }

  public void barWithLambdas() {
    System.out.println("mutate me");

    Runnable runnable = () -> {
      System.out.println("mutate me also in lambdas");

      Runnable anotherOne = () -> {
        System.out.println("mutate me also recursive lambdas");
      };
    };
  }
}


