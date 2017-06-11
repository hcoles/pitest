package org.pitest.mutationtest.build.intercept.javafeatures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassName;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.predicate.True;
import org.pitest.mutationtest.build.ClassTree;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.GregorMutater;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.config.Mutator;
import org.pitest.util.ResourceFolderByteArraySource;

public class TryWithResourcesFilterTest {

  private static final Collection<String> COMPILERS = Arrays.asList("javac",
      "ecj", "aspectj");
  private static final String             PATH      = "trywithresources/{0}_{1}";

  ClassByteArraySource source = new ResourceFolderByteArraySource();
  TryWithResourcesFilter testee = new TryWithResourcesFilter();
  GregorMutater mutator;
  
  @Before
  public void setup() {
    Collection<MethodMutatorFactory> mutators = Mutator.defaults();
    mutator = new GregorMutater(source, True.<MethodInfo>all(), mutators);
    testee = new TryWithResourcesFilter();
  }
  
  @Test
  public void shouldDeclareTypeAsFilter() {
    assertEquals(InterceptorType.FILTER, this.testee.type());
  }
  
  @Test
  public void shouldWorkWithTry() {
    for (String compiler : COMPILERS) {
      String clazz = MessageFormat.format(PATH, "TryExample", compiler);
      final Collection<MutationDetails> actualDetails = processWithTestee(clazz);
      assertThat(actualDetails)
      .describedAs("Wrong number of mutants  with " + compiler)
      .hasSize(1);
    }
  }
  
  @Test
  public void shouldWorkWithTryCatch() {
    for (String compiler : COMPILERS) {
      String clazz = MessageFormat.format(PATH, "TryCatchExample", compiler);
      final Collection<MutationDetails> actualDetails = processWithTestee(clazz);
      assertThat(actualDetails)
      .describedAs("Wrong number of mutants  with " + compiler)
      .hasSize(2);
    }
  }
  
  @Test
  public void shouldWorkWithTryWithInterface() {
    for (String compiler : COMPILERS) {
      String clazz = MessageFormat.format(PATH, "TryWithInterfaceExample", compiler);
      final Collection<MutationDetails> actualDetails = processWithTestee(clazz);
      assertThat(actualDetails)
      .describedAs("Wrong number of mutants  with " + compiler)
      .hasSize(1);
    }
  }
  
  @Test
  public void shouldWorkWithTryWithNestedTry() {
    for (String compiler : COMPILERS) {
      String clazz = MessageFormat.format(PATH, "TryWithNestedTryExample", compiler);
      final Collection<MutationDetails> actualDetails = processWithTestee(clazz);
      assertThat(actualDetails)
      .describedAs("Wrong number of mutants  with " + compiler)
      .hasSize(1);
    }
  }
  
  @Test
  public void shouldWorkWithTwoClosables() {
    for (String compiler : COMPILERS) {
      String clazz = MessageFormat.format(PATH, "TryWithTwoCloseableExample", compiler);
      final Collection<MutationDetails> actualDetails = processWithTestee(clazz);
      assertThat(actualDetails)
      .describedAs("Wrong number of mutants  with " + compiler)
      .hasSize(1);
    }
  }
  
  Collection<MutationDetails> processWithTestee(String clazz) {

    testee.begin(treeFor(clazz));
    Collection<MutationDetails> actual = testee.intercept(findMutationsFor(clazz), mutator);
    testee.end();
    return actual;
  }

  private FunctionalList<MutationDetails> findMutationsFor(String clazz) {
    FunctionalList<MutationDetails> mutations = mutator.findMutations(ClassName.fromString(clazz));
    assertThat(mutations).isNotEmpty();
    return mutations;
  }
  
  
  private ClassTree treeFor(String clazz) {
    return ClassTree.fromBytes(source.getBytes(clazz).value());
  }

}
