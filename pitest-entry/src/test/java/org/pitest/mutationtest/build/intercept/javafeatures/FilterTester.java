package org.pitest.mutationtest.build.intercept.javafeatures;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.assertj.core.api.Condition;
import org.assertj.core.api.SoftAssertions;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.MethodTree;
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
   
  
  public void assertFiltersMutationAtNLocations(int n, Class<?> clazz) {
    Sample s = makeSampleForCurrentCompiler(clazz);
    assertFiltersMutationAtNLocations(n, s, mutateFromClassLoader());
  }
  
  public void assertFiltersMutationAtNLocations(int n, Sample s, GregorMutater mutator) {
    List<MutationDetails> mutations = mutator.findMutations(s.className);
    Collection<MutationDetails> actual = filter(s.clazz, mutations, mutator);
   
    Set<Loc> originalLocations = new LinkedHashSet<>();
    FCollection.mapTo(mutations, toLocation(s.clazz), originalLocations);
    
    Set<Loc> filteredLocations = new LinkedHashSet<>();
    FCollection.mapTo(actual, toLocation(s.clazz), filteredLocations);
    
    assertThat(filteredLocations)
    .describedAs("Expected to filter %d locations from the %d in %s", n, originalLocations.size(), s.clazz.toString())
    .hasSize(originalLocations.size() - n);

  }
  
  private F<MutationDetails, Loc> toLocation(final ClassTree tree) {
    return new F<MutationDetails, Loc>() {
      @Override
      public Loc apply(MutationDetails a) {
        MethodTree method = tree.method(a.getId().getLocation()).value();
        Loc l = new Loc();
        l.index = a.getInstructionIndex();
        l.node = method.instructions().get(a.getInstructionIndex());
        return l;  
      }
      
    };
  }

  public void assertLeavesNMutants(int n, String sample) {
    GregorMutater mutator = mutateFromResourceDir();
    atLeastOneSampleExists(sample);
    
    SoftAssertions softly = new SoftAssertions();
    
    for (Sample s : samples(sample)) {
      List<MutationDetails> mutations = mutator.findMutations(s.className);
      Collection<MutationDetails> actual = filter(s.clazz, mutations, mutator);
      
      softly.assertThat(actual)
      .describedAs("Wrong number of mutants  with " + s.compiler)
      .hasSize(n);
    }
    
    softly.assertAll();
  }

  public void assertFiltersNMutationFromSample(int n, String sample) {
    GregorMutater mutator = mutateFromResourceDir();
    atLeastOneSampleExists(sample);
    
    SoftAssertions softly = new SoftAssertions();
    
    for (Sample s : samples(sample)) {
      assertFiltersNMutants(n, mutator, s, softly);
    }
    
    softly.assertAll();
  }
  
  public void assertFiltersNMutationFromClass(int n, Class<?> clazz) {
    Sample s = makeSampleForCurrentCompiler(clazz);
    
    SoftAssertions softly = new SoftAssertions();
    
    assertFiltersNMutants(n, mutateFromClassLoader(), s, softly);
    
    softly.assertAll();
  }

  private Sample makeSampleForCurrentCompiler(Class<?> clazz) {
    ClassloaderByteArraySource source = ClassloaderByteArraySource.fromContext();
    Sample s = new Sample();
    s.className = ClassName.fromClass(clazz);
    s.clazz = ClassTree.fromBytes(source.getBytes(clazz.getName()).value());
    s.compiler = "current";
    return s;
  }

  public void assertFiltersMutationsFromMutator(String id, Class<?> clazz) {
    Sample s = sampleForClass(clazz);
    GregorMutater mutator = mutateFromClassLoader();
    List<MutationDetails> mutations = mutator.findMutations(s.className);
    Collection<MutationDetails> actual = filter(s.clazz, mutations, mutator);
    
    SoftAssertions softly = new SoftAssertions();
    checkHasNMutants(1, s, softly, mutations);
    
    List<MutationDetails> filteredOut = FCollection.filter(mutations, notIn(actual));
    
    softly.assertThat(filteredOut).describedAs("No mutants filtered").isNotEmpty();
    softly.assertThat(filteredOut).have(mutatedBy(id));
    softly.assertAll();

  }

  private Condition<? super MutationDetails> mutatedBy(final String id) {
    return new  Condition<MutationDetails>() {
      @Override
      public boolean matches(MutationDetails value) {
        return value.getId().getMutator().equals(id);
      }
      
    };
  }

  private F<MutationDetails, Boolean> notIn(
      final Collection<MutationDetails> actual) {
    return new F<MutationDetails, Boolean>() {
      @Override
      public Boolean apply(MutationDetails a) {
        return !actual.contains(a);
      }
      
    };
  }

  private void assertFiltersNMutants(int n, GregorMutater mutator, Sample s, SoftAssertions softly) {
    List<MutationDetails> mutations = mutator.findMutations(s.className);
    Collection<MutationDetails> actual = filter(s.clazz, mutations, mutator);
    
    checkHasNMutants(n, s, softly, mutations);
    
    softly.assertThat(mutations.size() == 0 && n == 0)
    .describedAs("Expecting no mutations to be filtered, but none were produced")
    .isFalse();
    

    softly.assertThat(actual)
    .describedAs("Expected to filter out " + n + " mutants but filtered "
                  + (mutations.size() - actual.size()) + " for compiler " + s.compiler
                  + " " + s.clazz)
    .hasSize(mutations.size() - n);
  }

  private void checkHasNMutants(int n, Sample s, SoftAssertions softly,
      List<MutationDetails> mutations) {
    softly.assertThat(mutations.size()) 
    .describedAs("Fewer mutations produced than expected with " + s.compiler + ". This test has a bug in it.\n" + s.clazz)
    .isGreaterThanOrEqualTo(n);
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

  
  private Sample sampleForClass(Class<?> clazz) {
    ClassloaderByteArraySource source = ClassloaderByteArraySource.fromContext();
    Sample s = new Sample();
    s.className = ClassName.fromClass(clazz);
    s.clazz = ClassTree.fromBytes(source.getBytes(clazz.getName()).value());
    s.compiler = "current";
    return s;
  }
}

class Sample {
  ClassName className;
  String compiler;
  ClassTree clazz;
}
