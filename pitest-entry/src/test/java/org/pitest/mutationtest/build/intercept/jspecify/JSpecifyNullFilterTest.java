package org.pitest.mutationtest.build.intercept.jspecify;

import java.util.Optional;
import java.util.concurrent.Callable;

import com.example.jspecify.NullMarkedAndUnmarkedClassReturn;
import com.example.jspecify.NullMarkedPackageReturn;
import com.example.jspecify.NullUnmarkedClassReturn;
import com.example.jspecify.UnannotatedOuterWithNestedReturn;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;
import org.pitest.classinfo.ClassByteArraySource;
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
  public void filtersMutantsInMethodAnnotatedNullMarked() {
    v.forClass(NullMarkedMethodReturn.class).forAnyCode()
        .mutantsAreGenerated().allMutantsAreFiltered().verify();
  }

  @Test
  public void doesNotFilterMethodAnnotatedNullUnmarkedInNullMarkedClass() {
    v.forClass(NullUnmarkedMethodReturn.class).forAnyCode()
        .mutantsAreGenerated().noMutantsAreFiltered().verify();
  }

  @Test
  public void filtersMutantsInClassNestedInNullMarkedClass() {
    v.forClass(NullMarkedOuter.Nested.class).forAnyCode()
        .mutantsAreGenerated().allMutantsAreFiltered().verify();
  }

  @Test
  public void filtersMutantsInClassDeeplyNestedInNullMarkedClass() {
    v.forClass(NullMarkedOuter.Nested.DeeplyNested.class).forAnyCode()
        .mutantsAreGenerated().allMutantsAreFiltered().verify();
  }

  @Test
  public void doesNotFilterNullUnmarkedClassNestedInNullMarkedClass() {
    v.forClass(NullMarkedOuter.UnmarkedNested.class).forAnyCode()
        .mutantsAreGenerated().noMutantsAreFiltered().verify();
  }

  @Test
  public void treatsClassWithBothAnnotationsAsUnannotated() {
    v.forClass(NullMarkedAndUnmarkedClassReturn.class).forAnyCode()
        .mutantsAreGenerated().allMutantsAreFiltered().verify();
  }

  @Test
  public void treatsClassWithBothAnnotationsAsUnannotatedOutsideMarkedPackage() {
    v.forClass(BothAnnotationsClassReturn.class).forAnyCode()
        .mutantsAreGenerated().noMutantsAreFiltered().verify();
  }

  @Test
  public void treatsMethodWithBothAnnotationsAsUnannotated() {
    v.forClass(BothAnnotationsMethodReturn.class).forAnyCode()
        .mutantsAreGenerated().allMutantsAreFiltered().verify();
  }

  @Test
  public void filtersNestedClassOfUnannotatedOuterInNullMarkedPackage() {
    v.forClass(UnannotatedOuterWithNestedReturn.Nested.class).forAnyCode()
        .mutantsAreGenerated().allMutantsAreFiltered().verify();
  }

  @Test
  public void doesNotFilterNestedClassOfUnannotatedOuterOutsideMarkedPackage() {
    v.forClass(UnannotatedOuter.Nested.class).forAnyCode()
        .mutantsAreGenerated().noMutantsAreFiltered().verify();
  }

  @Test
  public void filtersMutantsInNullMarkedModule() {
    verifierWithModuleInfo(true).forClass(UnannotatedOuter.Nested.class).forAnyCode()
        .mutantsAreGenerated().allMutantsAreFiltered().verify();
  }

  @Test
  public void doesNotFilterMutantsInModuleWithoutNullMarked() {
    verifierWithModuleInfo(false).forClass(UnannotatedOuter.Nested.class).forAnyCode()
        .mutantsAreGenerated().noMutantsAreFiltered().verify();
  }

  @Test
  public void doesNotFilterNullUnmarkedClassInNullMarkedModule() {
    verifierWithModuleInfo(true).forClass(NullMarkedOuter.UnmarkedNested.class)
        .forAnyCode().mutantsAreGenerated().noMutantsAreFiltered().verify();
  }

  private static InterceptorVerifier verifierWithModuleInfo(boolean nullMarked) {
    final byte[] moduleInfo = moduleInfo(nullMarked);
    return VerifierStart
        .forInterceptor(new JSpecifyNullFilter(ClassloaderByteArraySource.fromContext(),
            clazz -> Optional.of(moduleInfo)))
        .usingMutator(NullReturnValsMutator.NULL_RETURNS);
  }

  private static byte[] moduleInfo(boolean nullMarked) {
    final ClassWriter cw = new ClassWriter(0);
    cw.visit(Opcodes.V11, Opcodes.ACC_MODULE, "module-info", null, null, null);
    cw.visitModule("test.module", 0, null).visitEnd();
    if (nullMarked) {
      cw.visitAnnotation("Lorg/jspecify/annotations/NullMarked;", true).visitEnd();
    }
    cw.visitEnd();
    return cw.toByteArray();
  }

  @Test
  public void fallsBackToPackageWhenOuterClassCannotBeLoaded() {
    final ClassByteArraySource source = ClassloaderByteArraySource.fromContext();
    final InterceptorVerifier noOuterClasses = VerifierStart
        .forInterceptor(new JSpecifyNullFilter(clazz ->
            clazz.equals(UnannotatedOuterWithNestedReturn.class.getName())
                ? Optional.empty() : source.getBytes(clazz)))
        .usingMutator(NullReturnValsMutator.NULL_RETURNS);
    noOuterClasses.forClass(UnannotatedOuterWithNestedReturn.Nested.class).forAnyCode()
        .mutantsAreGenerated().allMutantsAreFiltered().verify();
  }

  @Test
  public void filtersMutantsInAnonymousClassInNullMarkedClass() {
    v.forClass(NullMarkedOuter.anonymous()).forAnyCode()
        .mutantsAreGenerated().allMutantsAreFiltered().verify();
  }

  @Test
  public void doesNotFilterAnonymousClassInNullUnmarkedClass() {
    v.forClass(NullMarkedOuter.UnmarkedNested.anonymous()).forAnyCode()
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

  private static class NullMarkedMethodReturn implements Callable<Object> {
    @NullMarked
    @Override
    public Object call() {
      return "";
    }
  }

  @NullMarked
  private static class NullUnmarkedMethodReturn implements Callable<Object> {
    @NullUnmarked
    @Override
    public Object call() {
      return "";
    }
  }

  @NullMarked
  private static class NullMarkedOuter {
    static Class<?> anonymous() {
      return new Callable<Object>() {
        @Override
        public Object call() {
          return "";
        }
      }.getClass();
    }

    static class Nested implements Callable<Object> {
      @Override
      public Object call() {
        return "";
      }

      static class DeeplyNested implements Callable<Object> {
        @Override
        public Object call() {
          return "";
        }
      }
    }

    @NullUnmarked
    static class UnmarkedNested implements Callable<Object> {
      static Class<?> anonymous() {
        return new Callable<Object>() {
          @Override
          public Object call() {
            return "";
          }
        }.getClass();
      }

      @Override
      public Object call() {
        return "";
      }
    }
  }

  private static class UnannotatedOuter {
    static class Nested implements Callable<Object> {
      @Override
      public Object call() {
        return "";
      }
    }
  }

  @NullMarked
  @NullUnmarked
  private static class BothAnnotationsClassReturn implements Callable<Object> {
    @Override
    public Object call() {
      return "";
    }
  }

  @NullMarked
  private static class BothAnnotationsMethodReturn implements Callable<Object> {
    @NullUnmarked
    @NullMarked
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
