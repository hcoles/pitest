package org.pitest.mutationtest.build.intercept.annotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.pitest.mutationtest.engine.MutationDetailsMother.aMutationDetail;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.functional.predicate.True;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.GregorMutater;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator;

public class ExcludedAnnotationInterceptorTest {

  ExcludedAnnotationInterceptor testee = new ExcludedAnnotationInterceptor(Arrays.asList("TestGeneratedAnnotation", "AnotherTestAnnotation"));
  Mutater mutator;
  
  @Before
  public void setUp() {
    ClassloaderByteArraySource source = ClassloaderByteArraySource.fromContext();
    Collection<MethodMutatorFactory> mutators = Collections.singleton((MethodMutatorFactory)VoidMethodCallMutator.VOID_METHOD_CALL_MUTATOR);
    mutator = new GregorMutater(source, True.<MethodInfo>all(), mutators);
  }
    
  
  @Test
  public void shouldDeclareSelfAsFilter() {
    assertThat(testee.type()).isEqualTo(InterceptorType.FILTER);
  }
  
  @Test
  public void shouldNotFilterMutationsWhenNoAnnotations() {
    Collection<MutationDetails> input = someMutations();
    Collection<MutationDetails> actual = runWithTestee(input, UnAnnotated.class);
    assertThat(actual).containsExactlyElementsOf(input);
  }

  @Test
  public void shouldFilterAllMutationsForClassesWithGeneratedAnnotation() {
    Collection<MutationDetails> actual = runWithTestee(someMutations(), AnnotatedWithGenerated.class);
    assertThat(actual).isEmpty();
  }
  
  @Test
  public void shouldFilterAllMutationsForClassesWithDoNoMutateAnnotation() {
    Collection<MutationDetails> actual = runWithTestee(someMutations(), AnnotatedWithDoNotMutate.class);
    assertThat(actual).isEmpty();
  }
  
  @Test
  public void shouldFilterMethodsWithGeneratedAnnotation() {
    List<MutationDetails> mutations = mutator.findMutations(ClassName.fromClass(MethodAnnotatedWithGenerated.class));
    Collection<MutationDetails> actual = runWithTestee(mutations, MethodAnnotatedWithGenerated.class);
    assertThat(actual).hasSize(1);
    assertThat(actual.iterator().next().getId().getLocation().getMethodName().name()).isEqualTo("bar");
  }
  
  private Collection<MutationDetails> runWithTestee(
      Collection<MutationDetails> input, Class<?> clazz) {
    testee.begin(treeFor(clazz));
    Collection<MutationDetails> actual = testee.intercept(input, mutator);
    return actual;
  }
  
  private Collection<MutationDetails> someMutations() {
    return aMutationDetail().build(2);
  }

  ClassTree treeFor(Class<?> clazz) {
    ClassloaderByteArraySource source = ClassloaderByteArraySource.fromContext();
    return ClassTree.fromBytes(source.getBytes(clazz.getName()).value());
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


