package org.pitest.mutationtest.build;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.mutationtest.EngineArguments;
import org.pitest.mutationtest.MutationConfig;
import org.pitest.mutationtest.build.intercept.javafeatures.ForEachFilterTest.HasForEachLoop;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.config.SettingsFactory;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.engine.gregor.CoverageIgnore;
import org.pitest.mutationtest.engine.gregor.DoNotMutate;
import org.pitest.mutationtest.engine.gregor.Generated;
import org.pitest.mutationtest.engine.gregor.config.GregorEngineFactory;
import org.pitest.util.ResourceFolderByteArraySource;

/**
 * Tests discovery of mutants - including the full default interceptor chain
 */
public class MutationDiscoveryTest {

  ReportOptions data = new ReportOptions();
  ClassByteArraySource cbas = new ResourceFolderByteArraySource();


  @Before
  public void setUp() {
    this.data.setTargetClasses(Collections.singleton("com.example.*"));
  }

  @Test
  public void shouldFilterMutantsInTryCatchFinallyCompiledWithJavaC() {
    this.data.setDetectInlinedCode(true);

    final ClassName clazz = ClassName.fromString("trywithresources/TryCatchFinallyExample_javac");
    final Collection<MutationDetails> actual = findMutants(clazz);
    assertThat(actual).hasSize(3);
  }

  @Test
  public void shouldFilterMutantsInTryCatchFinallyCompiledWithEcj() {
    this.data.setDetectInlinedCode(true);

    final ClassName clazz = ClassName.fromString("trywithresources/TryCatchFinallyExample_ecj");
    final Collection<MutationDetails> actual = findMutants(clazz);
    assertThat(actual).hasSize(3);
  }

  @Test
  public void shouldFilterMutantsInTryCatchFinallyCompiledWithAspectJ() {
    this.data.setDetectInlinedCode(true);

    final ClassName clazz = ClassName.fromString("trywithresources/TryCatchFinallyExample_aspectj");
    final Collection<MutationDetails> actual = findMutants(clazz);
    assertThat(actual).hasSize(3);
  }

  @Test
  public void shouldFilterMutantsInTryFinallyCompiledWithJavaC() {
    this.data.setDetectInlinedCode(true);

    final ClassName clazz = ClassName.fromString("trywithresources/TryFinallyExample_javac");
    final Collection<MutationDetails> actual = findMutants(clazz);
    assertThat(actual).hasSize(2);
  }

  @Test
  public void shouldFilterMutantsInTryFinallyCompiledWithEcj() {
    this.data.setDetectInlinedCode(true);

    final ClassName clazz = ClassName.fromString("trywithresources/TryFinallyExample_ecj");
    final Collection<MutationDetails> actual = findMutants(clazz);
    assertThat(actual).hasSize(2);
  }

  @Test
  public void shouldFilterMutantsInTryFinallyCompiledWithAspectJ() {
    this.data.setDetectInlinedCode(true);

    final ClassName clazz = ClassName.fromString("trywithresources/TryFinallyExample_aspectj");
    final Collection<MutationDetails> actual = findMutants(clazz);
    assertThat(actual).hasSize(2);
  }

  @Test
  public void shouldNotFilterInlinedFinallyBlocksWhenFlagNotSet() {
    final ClassName clazz = ClassName.fromString("trywithresources/TryCatchFinallyExample_javac");

    this.data.setDetectInlinedCode(true);
    final Collection<MutationDetails> filtered = findMutants(clazz);

    this.data.setDetectInlinedCode(false);
    final Collection<MutationDetails> unfiltered = findMutants(clazz);

    assertThat(filtered.size()).isLessThan(unfiltered.size());
  }

  @Test
  public void shouldFilterMutantsInTryWithResourcesClosableCompiledWithJavac() {
    final ClassName clazz = ClassName.fromString("trywithresources/TryWithTwoCloseableExample_javac");
    final Collection<MutationDetails> actual = findMutants(clazz);
    assertThat(actual).hasSize(1);
  }

  @Test
  public void shouldFilterMutantsInTryWithResourcesClosableCompiledWithEcj() {
    final ClassName clazz = ClassName.fromString("trywithresources/TryWithTwoCloseableExample_ecj");
    final Collection<MutationDetails> actual = findMutants(clazz);
    assertThat(actual).hasSize(1);
  }

  @Test
  public void shouldFilterMutantsInTryWithResourcesClosableCompiledWithApectj() {
    final ClassName clazz = ClassName.fromString("trywithresources/TryWithTwoCloseableExample_aspectj");
    final Collection<MutationDetails> actual = findMutants(clazz);
    assertThat(actual).hasSize(1);
  }

  @Test
  public void shouldFilterMutationsInLoggingCalls() {
    this.data.setLoggingClasses(Collections.singleton("java.util.logging"));
    final Collection<MutationDetails>  actual = findMutants(HasLogger.class);
    assertThat(actual).isEmpty();
  }


  @Test
  public void shouldNotMutateMethodsAnnotatedWithGenerated() {
    final Collection<MutationDetails> actualDetails = findMutants(AnnotatedToAvoidMethod.class);
    // all but two methods are annotated to ignore
    assertEquals(2, actualDetails.size());
  }

  @Test
  public void shouldFilterImplicitNullChecksInLambdas() {
    final ClassName clazz = ClassName.fromString("implicitnullcheck/RemovedCallBug_javac");

    this.data.setMutators(Collections.singletonList("ALL"));

    final Collection<MutationDetails> foundByDefault = findMutants(clazz);

    this.data.setFeatures(Collections.singletonList("-FINULL"));

    final Collection<MutationDetails> foundWhenDisabled = findMutants(clazz);

    assertThat(foundWhenDisabled.size()).isGreaterThan(foundByDefault.size());
  }

  @Test
  public void shouldFilterMutationsToForLoopIncrements() {
    final Collection<MutationDetails>  actual = findMutants(HasForLoop.class);

    this.data.setFeatures(Collections.singletonList("-FFLOOP"));
    final Collection<MutationDetails> actualWithoutFilter = findMutants(HasForLoop.class);

    assertThat(actual.size()).isLessThan(actualWithoutFilter.size());
  }

  @Test
  public void shouldFilterMutationsToForEachLoops() {
    final Collection<MutationDetails>  actual = findMutants(HasForEachLoop.class);

    this.data.setFeatures(Collections.singletonList("-FFEACH"));
    final Collection<MutationDetails> actualWithoutFilter = findMutants(HasForEachLoop.class);

    assertThat(actual.size()).isLessThan(actualWithoutFilter.size());
  }


  @Test
  public void filtersEquivalentReturnValsMutants() {
    this.data.setMutators(Collections.singletonList("PRIMITIVE_RETURNS"));
    final Collection<MutationDetails>  actual = findMutants(AlreadyReturnsConstZero.class);
    assertThat(actual).isEmpty();
  }

  public static class AnnotatedToAvoidMethod {
    public int a() {
      return 1;
    }

    @Generated
    public int b() {
      return 1;
    }

    @DoNotMutate
    public int c() {
      return 1;
    }

    @CoverageIgnore
    public int d() {
      return 1;
    }

    public int e() {
      return 1;
    }
  }

  private static class HasLogger {
    private static Logger log = Logger.getLogger(HasLogger.class.getName());

    @SuppressWarnings("unused")
    public void call(int i) {
      log.info("foo " + i);
    }
  }


  private Collection<MutationDetails> findMutants(Class<?> clazz) {
    this.data.setTargetClasses(Collections.singleton(clazz.getName()));
    this.cbas = ClassloaderByteArraySource.fromContext();
    return findMutants(ClassName.fromClass(clazz));
  }

  private Collection<MutationDetails> findMutants(ClassName clazz) {
    final MutationSource source = createSource(this.cbas);
    return source.createMutations(clazz);
  }

  MutationSource createSource(ClassByteArraySource source) {
    final SettingsFactory settings = new SettingsFactory(this.data,
        PluginServices.makeForContextLoader());
    final MutationInterceptor interceptor = settings.getInterceptor()
        .createInterceptor(this.data, source);

    final MutationEngine engine = new GregorEngineFactory().createEngine(
        EngineArguments.arguments().withExcludedMethods(this.data.getExcludedMethods())
        .withMutators(this.data.getMutators()));

    final MutationConfig config = new MutationConfig(engine, null);

    return new MutationSource(config, noTestPrioritisation(), source,
        interceptor);
  }

  private TestPrioritiser noTestPrioritisation() {
    return mutation -> Collections.emptyList();
  }

  static class HasForLoop {
    public void foo() {
      for (int i = 0; i != 10; i++) {
        System.out.println(i);
      }
    }
  }

  class AlreadyReturnsConstZero {
    public int a() {
      return 0;
    }
  }
}
