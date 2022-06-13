package org.pitest.mutationtest.build.intercept.staticinitializers;

import com.example.staticinitializers.SingletonWithWorkInInitializer;
import org.junit.Test;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.mutators.NullMutateEverything;
import org.pitest.verifier.interceptors.InterceptorVerifier;
import org.pitest.verifier.interceptors.VerifierStart;

import java.util.function.Predicate;


public class StaticInitializerInterceptorTest {

    InterceptorVerifier v = VerifierStart.forInterceptorFactory(new StaticInitializerInterceptorFactory())
            .usingMutator(new NullMutateEverything());


    @Test
    public void shouldNotFilterMutationsInClassWithoutStaticInitializer() {
        v.forClass(NoStaticInializer.class)
                .forAnyCode()
                .mutantsAreGenerated()
                .noMutantsAreFiltered()
                .verify();
    }

    @Test
    public void shouldFilterMutationsInStaticInitializer() {
      v.forClass(HasStaticInializer.class)
              .forMethod("<clinit>")
              .forAnyCode()
              .mutantsAreGenerated()
              .allMutantsAreFiltered()
              .verify();
    }

    @Test
    public void shouldMarkMutationsInPrivateMethodsCalledFromStaticInitializer() {
      v.forClass(HasPrivateCallsFromStaticInializer.class)
              .forMethod("a")
              .forAnyCode()
              .mutantsAreGenerated()
              .allMutantsAreFiltered()
              .verify();
    }

    @Test
    public void shouldNotMarkMutationsInPackageDefaultMethodsCalledFromStaticInitializer() {
      v.forClass(HasDefaultCallsFromStaticInializer.class)
              .forMethod("a")
              .forAnyCode()
              .mutantsAreGenerated()
              .noMutantsAreFiltered()
              .verify();
    }


    @Test
    public void shouldNotMarkMutationsInPrivateStaticMethodsNotInvolvedInInit() {
      v.forClass(HasOtherPrivateStaticMethods.class)
              .forMethod("b")
              .forAnyCode()
              .mutantsAreGenerated()
              .noMutantsAreFiltered()
              .verify();
    }

    @Test
    public void shouldNotMarkMutationsInOverriddenMethodsNotInvolvedInStaticInit() {
      v.forClass(HasOverloadedMethodsThatAreNotUsedInStaticInitialization.class)
              .forMutantsMatching(inMethod("a", "(I)V"))
              .mutantsAreGenerated()
              .noMutantsAreFiltered()
              .verify();
    }

  @Test
  public void filtersMutantsInSingletonConstructor() {
    v.forClass(SingletonWithWorkInInitializer.class)
            .forMutantsMatching(inMethod("<init>", "()V"))
            .mutantsAreGenerated()
            .allMutantsAreFiltered()
            .verify();
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

