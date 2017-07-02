package org.pitest.mutationtest.build.intercept.staticinitializers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.predicate.True;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.GregorMutater;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator;

public class StaticInitializerInterceptorTest {
  
  StaticInitializerInterceptor testee;
  GregorMutater mutator;
  
  @Before
  public void setup() {
    ClassloaderByteArraySource source = ClassloaderByteArraySource.fromContext();
    Collection<MethodMutatorFactory> mutators = Collections.singleton((MethodMutatorFactory)VoidMethodCallMutator.VOID_METHOD_CALL_MUTATOR);
    mutator = new GregorMutater(source, True.<MethodInfo>all(), mutators);
    testee = new StaticInitializerInterceptor();
  }

  @Test
  public void shouldNotMarkAnyMutationsInClassWithoutStaticInitializer() {
    Class<?> clazz = NoStaticInializer.class;
    FunctionalList<MutationDetails> mutations = findMutationsFor(clazz); 
    
    testee.begin(treeFor(clazz));
    Collection<MutationDetails> actual = testee.intercept(mutations, mutator);
    testee.end();
    
    assertThat(actual).isSameAs(mutations);
  }

  @Test
  public void shouldMarkMutationsInStaticInitializer() {
    Collection<MutationDetails> actual = processWithTestee(HasStaticInializer.class);  
    assertAllMarkedAsInStaticInitializers(actual);
  }

  @Test
  public void shouldMarkMutationsInPrivateMethodsCalledFromStaticInitializer() {  
    Collection<MutationDetails> actual = processWithTestee(HasPrivateCallsFromStaticInializer.class);  
    assertAllMarkedAsInStaticInitializers(actual);
  }
  
  @Test
  public void shouldNotMarkMutationsInPackageDefaultMethodsCalledFromStaticInitializer() {
    Collection<MutationDetails> actual = processWithTestee(HasDefaultCallsFromStaticInializer.class);  
    assertOnlyClinitMethodsMarked(actual);
  }  
  

  @Test
  public void shouldNotMarkMutationsInPrivateStaticMethodsNotInvolvedInInit() {
    Collection<MutationDetails> actual = processWithTestee(HasOtherPrivateStaticMethods.class);  
    assertOnlyClinitMethodsMarked(actual);
  } 
  
  @Test
  public void shouldNotMarkMutationsInOverriddenMethodsNotInvolvedInStaticInit() {
    Collection<MutationDetails> actual = processWithTestee(HasOverloadedMethodsThatAreNotUsedInStaticInitialization.class);  
    assertOnlyClinitMethodsMarked(actual);
  } 
  
  Collection<MutationDetails> processWithTestee(Class<?> clazz) {
    testee.begin(treeFor(clazz));
    Collection<MutationDetails> actual = testee.intercept(findMutationsFor(clazz), mutator);
    testee.end();
    return actual;
  }

  private FunctionalList<MutationDetails> findMutationsFor(Class<?> clazz) {
    FunctionalList<MutationDetails> mutations = mutator.findMutations(ClassName.fromClass(clazz));
    assertThat(mutations).isNotEmpty();
    return mutations;
  }
  
  
  private ClassTree treeFor(Class<?> clazz) {
    ClassloaderByteArraySource source = ClassloaderByteArraySource.fromContext();
    return ClassTree.fromBytes(source.getBytes(clazz.getName()).value());
  }

  
  
  private void assertAllMarkedAsInStaticInitializers(
      Collection<MutationDetails> actual) {
    for (MutationDetails each : actual ) {
      if (!each.isInStaticInitializer()) {
        fail("Expected all mutants to be marked as for static initialization but " + each + " was not");
      }
    }
    
  }
  
  private void assertOnlyClinitMethodsMarked(Collection<MutationDetails> actual) {
    for (MutationDetails each : actual ) {
      if (each.isInStaticInitializer()) {
        if (!each.getId().getLocation().getMethodName().name().equals("<clinit>")) { 
          fail("Expected no mutants to be marked as for static initialization but " + each + " was");
        }
      }
    }
    
  }

}

class NoStaticInializer {
  {
    System.out.println("NOT static code");
  }
}

class HasStaticInializer {
  static {
    System.out.println("static code");
  }
}

class HasPrivateCallsFromStaticInializer {
  static {
    a();
  }
  
  private static void a() {
    System.out.println("static code");
  }
}

class HasDefaultCallsFromStaticInializer {
  static {
    a();
  }
  
  static void a() {
    System.out.println("NOT guaranteed to be static code");
  }
}

class HasOtherPrivateStaticMethods {
  static {
    a();
  }
  
  private static void a() {

  }
  
  public static void entryPoint(int i) {
    b(i);
  }
  

  private static void b(int i) {
    System.out.println("NOT static code");
  }
}


class HasOverloadedMethodsThatAreNotUsedInStaticInitialization {
  static {
    a();
  }
  
  private static void a() {

  }
  
  public static void entryPoint(int i) {
    a(i);
  }
  
  // same name, different sig
  private static void a(int i) {
    System.out.println("NOT static code");
  }
}

