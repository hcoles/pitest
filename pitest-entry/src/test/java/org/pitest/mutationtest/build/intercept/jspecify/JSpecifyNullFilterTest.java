package org.pitest.mutationtest.build.intercept.jspecify;

import java.util.concurrent.Callable;

import org.junit.Test;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.mutationtest.engine.gregor.mutators.returns.NullReturnValsMutator;
import org.pitest.verifier.interceptors.FactoryVerifier;
import org.pitest.verifier.interceptors.InterceptorVerifier;
import org.pitest.verifier.interceptors.VerifierStart;

public class JSpecifyNullFilterTest {

  private final MutationInterceptorFactory underTest = new JSpecifyNullFilterFactory();
  private final InterceptorVerifier v = VerifierStart
      .forInterceptor(new JSpecifyNullFilter(ClassloaderByteArraySource.fromContext()))
      .usingMutator(NullReturnValsMutator.NULL_RETURNS);

  @Test
  public void isOnChain() {
    FactoryVerifier.confirmFactory(underTest).isOnChain();
  }

  @Test
  public void isOnByDefault() {
    FactoryVerifier.confirmFactory(underTest).isOnByDefault();
  }

  @Test
  public void createsFilters() {
    FactoryVerifier.confirmFactory(underTest)
        .createsInterceptorsOfType(InterceptorType.FILTER);
  }

  @Test
  public void filtersMutantsInClassAnnotatedNullMarked() {
    v.forClass(NullMarkedClassReturn.class).forAnyCode()
        .mutantsAreGenerated().allMutantsAreFiltered().verify();
  }

  @Test
  public void doesNotFilterNullableReturnTypeInNullMarkedClass() {
    v.forClass(NullMarkedNullableReturn.class).forAnyCode()
        .mutantsAreGenerated().noMutantsAreFiltered().verify();
  }

  @Test
  public void filtersMutantsInNullMarkedPackage() {
    v.forClass(NullMarkedPackageReturn.class).forAnyCode()
        .mutantsAreGenerated().allMutantsAreFiltered().verify();
  }

  @Test
  public void doesNotFilterClassOptingOutOfNullMarkedPackage() {
    v.forClass(NullUnmarkedClassReturn.class).forAnyCode()
        .mutantsAreGenerated().noMutantsAreFiltered().verify();
  }

  @Test
  public void doesNotFilterWhenNothingIsMarked() {
    v.forClass(java.util.concurrent.atomic.AtomicReference.class).forAnyCode()
        .noMutantsAreFiltered().verify();
  }

  @NullMarked
  private static class NullMarkedClassReturn implements Callable<Object> {
    @Override
    public Object call() {
      return "";
    }
  }

  @NullMarked
  private static class NullMarkedNullableReturn implements Callable<Object> {
    @Override
    public @Nullable Object call() {
      return "";
    }
  }
}
