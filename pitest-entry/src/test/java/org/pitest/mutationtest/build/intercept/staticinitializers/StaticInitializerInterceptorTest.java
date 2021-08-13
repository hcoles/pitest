package org.pitest.mutationtest.build.intercept.staticinitializers;

import org.junit.Test;
import org.pitest.mutationtest.build.intercept.javafeatures.FilterTester;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.mutators.NullMutateEverything;

import java.util.function.Predicate;


public class StaticInitializerInterceptorTest {

  StaticInitializerInterceptor testee = new StaticInitializerInterceptor();

  FilterTester verifier = new FilterTester("", this.testee, NullMutateEverything.asList());


  @Test
  public void shouldNotFilterMutationsInClassWithoutStaticInitializer() {
    verifier.assertFiltersNMutationFromClass(0, NoStaticInializer.class);
  }

  @Test
  public void shouldFilterMutationsInStaticInitializer() {
    verifier.assertFiltersMutationsMatching(inMethodNamed("<clinit>"), HasStaticInializer.class);
  }

  @Test
  public void shouldMarkMutationsInPrivateMethodsCalledFromStaticInitializer() {
    verifier.assertFiltersMutationsMatching(inMethodNamed("a"), HasPrivateCallsFromStaticInializer.class);
  }

  @Test
  public void shouldNotMarkMutationsInPackageDefaultMethodsCalledFromStaticInitializer() {
    verifier.assertFiltersNoMutationsMatching(inMethodNamed("a"), HasDefaultCallsFromStaticInializer.class);
  }


  @Test
  public void shouldNotMarkMutationsInPrivateStaticMethodsNotInvolvedInInit() {
    verifier.assertFiltersNoMutationsMatching(inMethodNamed("b"), HasOtherPrivateStaticMethods.class);
  }

  @Test
  public void shouldNotMarkMutationsInOverriddenMethodsNotInvolvedInStaticInit() {
    verifier.assertFiltersNoMutationsMatching(inMethod("a", "(I)V"), HasOverloadedMethodsThatAreNotUsedInStaticInitialization.class);
  }

  private Predicate<MutationDetails> inMethodNamed(String name) {
    return m -> m.getMethod().equals(name);
  }

  private Predicate<MutationDetails> inMethod(String name, String desc) {
    return m -> m.getMethod().equals(name) && m.getId().getLocation().getMethodDesc().equals(desc);
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

