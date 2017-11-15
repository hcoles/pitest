package org.pitest.mutationtest.build.intercept.javafeatures;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.functional.predicate.True;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.GregorMutater;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.util.ResourceFolderByteArraySource;

public class FilterTester {
  
  private static final Collection<String> COMPILERS = Arrays.asList("javac",
      "ecj", "aspectj");
 
  private final String path;
  private final ClassByteArraySource source = new ResourceFolderByteArraySource();
  private final MutationInterceptor testee;
  private final Collection<MethodMutatorFactory> mutators;
  
  public FilterTester(String path, MutationInterceptor testee, MethodMutatorFactory ... mutators) {
    this(path, testee, Arrays.asList(mutators));
  }
   
  public FilterTester(String path, MutationInterceptor testee, Collection<MethodMutatorFactory> mutators) {
    this.mutators = mutators;
    this.testee = testee;
    this.path = path;
  }
   
  
  public void assertLeavesNMutants(int n, String sample) {
    GregorMutater mutator = mutateFromResourceDir();
    atLeastOneSampleExists(sample);
    for (Sample s : samples(sample)) {
      List<MutationDetails> mutations = mutator.findMutations(s.className);
      Collection<MutationDetails> actual = filter(s.clazz, mutations, mutator);
      
      assertThat(actual)
      .describedAs("Wrong number of mutants  with " + s.compiler)
      .hasSize(n);
    }
    
  }

  public void assertFiltersNMutationFromSample(int n, String sample) {
    GregorMutater mutator = mutateFromResourceDir();
    atLeastOneSampleExists(sample);
    for (Sample s : samples(sample)) {
      assertFiltersNMutants(n, mutator, s);
    }
  }
  
  public void assertFiltersNMutationFromClass(int n, Class<?> clazz) {
    ClassloaderByteArraySource source = ClassloaderByteArraySource.fromContext();
    Sample s = new Sample();
    s.className = ClassName.fromClass(clazz);
    s.clazz = ClassTree.fromBytes(source.getBytes(clazz.getName()).value());
    s.compiler = "current";
    assertFiltersNMutants(n, mutateFromClassLoader(), s);
  }



  private void assertFiltersNMutants(int n, GregorMutater mutator, Sample s) {
    List<MutationDetails> mutations = mutator.findMutations(s.className);
    Collection<MutationDetails> actual = filter(s.clazz, mutations, mutator);
    
    assertThat(mutations.size()) 
    .describedAs("Fewer mutations produced than expected with " + s.compiler + ". This test has a bug in it.\n" + s.clazz)
    .isGreaterThanOrEqualTo(n);
    
    assertThat(mutations.size() == 0 && n == 0)
    .describedAs("Expecting no mutations to be filtered, but none were produced")
    .isFalse();
    

    assertThat(actual)
    .describedAs("Expected to filter out " + n + " mutants but fitlered "
                  + (mutations.size() - actual.size()) + " for compiler " + s.compiler
                  + " " + s.clazz)
    .hasSize(mutations.size() - n);
  }

  private GregorMutater mutateFromResourceDir() {
    return new GregorMutater(source, True.<MethodInfo>all(), mutators);
  }
  
  private GregorMutater mutateFromClassLoader() {
    return new GregorMutater( ClassloaderByteArraySource.fromContext(), True.<MethodInfo>all(), mutators);
  }
  
  
  private String makeClassName(String sample, String compiler) {
    String clazz = MessageFormat.format(path, sample, compiler);
    return clazz;
  }
  
  
  private List<Sample> samples(final String sample) {
    F<String, Option<Sample>> toPair = new F<String, Option<Sample>>() {
      @Override
      public Option<Sample> apply(String compiler) {
        String clazz = makeClassName(sample, compiler);
        Option<byte[]> bs = source.getBytes(clazz);
        if (bs.hasSome()) {
          Sample p = new Sample();
          p.className = ClassName.fromString(clazz);
          p.clazz = ClassTree.fromBytes(bs.value());
          p.compiler = compiler;
          return Option.some(p);
        }
        return Option.none();

      }
      
    };
    return FCollection.flatMap(COMPILERS, toPair);
  }
  
  private boolean atLeastOneSampleExists(String sample) {
    for (String compiler : COMPILERS) {
      String clazz = makeClassName(sample, compiler);
      if (source.getBytes(clazz).hasSome()) {
        return true;
      }
    }
    throw new RuntimeException("No samples found for any compiler for " + sample);
  }
  
  private Collection<MutationDetails> filter(ClassTree clazz,
      List<MutationDetails> mutations, Mutater mutator) {
    testee.begin(clazz);
    Collection<MutationDetails> actual = testee.intercept(mutations, mutator);
    testee.end();
    return actual;
  }

}

class Sample {
  ClassName className;
  String compiler;
  ClassTree clazz;
}
