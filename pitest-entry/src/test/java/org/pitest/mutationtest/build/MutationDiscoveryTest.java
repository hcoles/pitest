package org.pitest.mutationtest.build;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassPathRoot;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.classpath.CodeSource;
import org.pitest.classpath.PathFilter;
import org.pitest.classpath.ProjectClassPaths;
import org.pitest.coverage.TestInfo;
import org.pitest.functional.predicate.Predicate;
import org.pitest.functional.predicate.True;
import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.MutationConfig;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.config.SettingsFactory;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.engine.gregor.config.GregorEngineFactory;
import org.pitest.mutationtest.filter.MutationFilter;
import org.pitest.util.Glob;
import org.pitest.util.ResourceFolderByteArraySource;

/**
 * Tests discovery of mutants - including the full default interceptor chain
 */
public class MutationDiscoveryTest {
  
  ReportOptions data = new ReportOptions();
  ClassByteArraySource cbas = new ResourceFolderByteArraySource();
  
  
  @Before
  public void setUp() {
    Predicate<String> match = new Glob("com.example.*");
    data.setTargetClasses(Collections.singleton(match));
  }

  @Test
  public void shouldFilterMutantsInTryCatchFinallyCompiledWithJavaC() {
    data.setDetectInlinedCode(true);  

    ClassName clazz = ClassName.fromString("trywithresources/TryCatchFinallyExample_javac");
    Collection<MutationDetails> actual = findMutants(clazz);
    assertThat(actual).hasSize(3);
  }
  
  @Test
  public void shouldFilterMutantsInTryCatchFinallyCompiledWithEcj() {
    data.setDetectInlinedCode(true);  

    ClassName clazz = ClassName.fromString("trywithresources/TryCatchFinallyExample_ecj");
    Collection<MutationDetails> actual = findMutants(clazz);
    assertThat(actual).hasSize(3);
  }
  
  @Test
  public void shouldFilterMutantsInTryCatchFinallyCompiledWithAspectJ() {
    data.setDetectInlinedCode(true);  

    ClassName clazz = ClassName.fromString("trywithresources/TryCatchFinallyExample_aspectj");
    Collection<MutationDetails> actual = findMutants(clazz);
    assertThat(actual).hasSize(3);
  }
 
  @Test
  public void shouldFilterMutantsInTryFinallyCompiledWithJavaC() {
    data.setDetectInlinedCode(true);  

    ClassName clazz = ClassName.fromString("trywithresources/TryFinallyExample_javac");
    Collection<MutationDetails> actual = findMutants(clazz);
    assertThat(actual).hasSize(2);
  }
  
  @Test
  public void shouldFilterMutantsInTryFinallyCompiledWithEcj() {
    data.setDetectInlinedCode(true);  

    ClassName clazz = ClassName.fromString("trywithresources/TryFinallyExample_ecj");
    Collection<MutationDetails> actual = findMutants(clazz);
    assertThat(actual).hasSize(2);
  }
  
  @Test
  public void shouldFilterMutantsInTryFinallyCompiledWithAspectJ() {
    data.setDetectInlinedCode(true);  

    ClassName clazz = ClassName.fromString("trywithresources/TryFinallyExample_aspectj");
    Collection<MutationDetails> actual = findMutants(clazz);
    assertThat(actual).hasSize(2);
  }
  
  @Test
  public void shouldNotFilterInlinedFinallyBlocksWhenFlagNotSet() {
    ClassName clazz = ClassName.fromString("trywithresources/TryCatchFinallyExample_javac");
    
    data.setDetectInlinedCode(true);
    Collection<MutationDetails> filtered = findMutants(clazz);
    
    data.setDetectInlinedCode(false);
    Collection<MutationDetails> unfiltered = findMutants(clazz);
    
    assertThat(filtered.size()).isLessThan(unfiltered.size());
  }
  
  @Test
  public void shouldFilterMutantsInTryWithResourcesClosableCompiledWithJavac() {
    ClassName clazz = ClassName.fromString("trywithresources/TryWithTwoCloseableExample_javac");
    Collection<MutationDetails> actual = findMutants(clazz);
    assertThat(actual).hasSize(1);
  }
  
  @Test
  public void shouldFilterMutantsInTryWithResourcesClosableCompiledWithEcj() {
    ClassName clazz = ClassName.fromString("trywithresources/TryWithTwoCloseableExample_ecj");
    Collection<MutationDetails> actual = findMutants(clazz);
    assertThat(actual).hasSize(1);
  }
  
  @Test
  public void shouldFilterMutantsInTryWithResourcesClosableCompiledWithApectj() {
    ClassName clazz = ClassName.fromString("trywithresources/TryWithTwoCloseableExample_aspectj");
    Collection<MutationDetails> actual = findMutants(clazz);
    assertThat(actual).hasSize(1);
  }
  
  @Test 
  public void shouldFilterMutationsInLoggingCalls() {
    data.setLoggingClasses(Collections.singleton("java.util.logging"));
    Collection<MutationDetails>  actual = findMutants(HasLogger.class);
    assertThat(actual).isEmpty();  
  }

  private static class HasLogger {
    private static Logger log = Logger.getLogger(HasLogger.class.getName());

    @SuppressWarnings("unused")
    public void call(int i) {
      log.info("foo " + i);
    }
  }

  
  private Collection<MutationDetails> findMutants(Class<HasLogger> clazz) {
    Predicate<String> glob = new Glob(clazz.getName());
    this.data.setTargetClasses(Collections.singleton(glob));
    this.cbas = ClassloaderByteArraySource.fromContext();
    return findMutants(ClassName.fromClass(clazz));
  }
  
  private Collection<MutationDetails> findMutants(ClassName clazz) {
    MutationSource source = createSource(cbas);
    return source.createMutations(clazz);
  }
  
  MutationSource createSource(ClassByteArraySource source) {
    final SettingsFactory settings = new SettingsFactory(data,
        PluginServices.makeForContextLoader());
    final MutationInterceptor interceptor = settings.getInterceptor()
        .createInterceptor(data, source);
    final PathFilter pf = new PathFilter(new True<ClassPathRoot>(),
        new True<ClassPathRoot>());
    final ProjectClassPaths cps = new ProjectClassPaths(data.getClassPath(),
        data.createClassesFilter(), pf);

    final CodeSource cs = new CodeSource(cps, null);

    final MutationEngine engine = new GregorEngineFactory().createEngine(
        Prelude.or(data.getExcludedMethods()), data.getMutators());
    
    final MutationConfig config = new MutationConfig(engine, null);

    final MutationFilter filter = settings.createMutationFilter()
        .createFilter(data.getFreeFormProperties(), cs, 0);
    return new MutationSource(config, filter, noTestPrioritisation(), source,
        interceptor);
  }

  private TestPrioritiser noTestPrioritisation() {
    return new TestPrioritiser() {
      @Override
      public List<TestInfo> assignTests(MutationDetails mutation) {
        return Collections.emptyList();
      }
    };
  }
}
