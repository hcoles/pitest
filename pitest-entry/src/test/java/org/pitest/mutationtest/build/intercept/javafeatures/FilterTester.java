package org.pitest.mutationtest.build.intercept.javafeatures;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.Condition;
import org.assertj.core.api.SoftAssertions;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.GregorMutater;
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
    final Sample s = makeSampleForCurrentCompiler(clazz);
    assertFiltersMutationAtNLocations(n, s, mutateFromClassLoader());
  }

  public void assertFiltersMutationAtNLocations(int n, Sample s, GregorMutater mutator) {
    final List<MutationDetails> mutations = mutator.findMutations(s.className);
    final Collection<MutationDetails> actual = filter(s.clazz, mutations, mutator);

    final Set<Loc> originalLocations = new LinkedHashSet<>();
    FCollection.mapTo(mutations, toLocation(s.clazz), originalLocations);

    final Set<Loc> filteredLocations = new LinkedHashSet<>();
    FCollection.mapTo(actual, toLocation(s.clazz), filteredLocations);

    assertThat(filteredLocations)
    .describedAs("Expected to filter %d locations from the %d in %s", n, originalLocations.size(), s.clazz.toString())
    .hasSize(originalLocations.size() - n);

  }

  private Function<MutationDetails, Loc> toLocation(final ClassTree tree) {
    return a -> {
      final MethodTree method = tree.method(a.getId().getLocation()).get();
      final Loc l = new Loc();
      l.index = a.getInstructionIndex();
      l.node = method.instruction(a.getInstructionIndex());
      return l;
    };
  }

  public void assertLeavesNMutants(int n, String sample) {
    final GregorMutater mutator = mutateFromResourceDir();
    atLeastOneSampleExists(sample);

    final SoftAssertions softly = new SoftAssertions();

    for (final Sample s : samples(sample)) {
      final List<MutationDetails> mutations = mutator.findMutations(s.className);
      final Collection<MutationDetails> actual = filter(s.clazz, mutations, mutator);

      softly.assertThat(actual)
      .describedAs("Wrong number of mutants  with " + s.compiler)
      .hasSize(n);
    }

    softly.assertAll();
  }

  public void assertFiltersNMutationFromSample(int n, String sample) {
    final GregorMutater mutator = mutateFromResourceDir();
    atLeastOneSampleExists(sample);

    final SoftAssertions softly = new SoftAssertions();

    for (final Sample s : samples(sample)) {
      assertFiltersNMutants(n, mutator, s, softly);
    }

    softly.assertAll();
  }

  public void assertFiltersNMutationFromClass(int n, Class<?> clazz) {
    final Sample s = makeSampleForCurrentCompiler(clazz);

    final SoftAssertions softly = new SoftAssertions();

    assertFiltersNMutants(n, mutateFromClassLoader(), s, softly);

    softly.assertAll();
  }

  private Sample makeSampleForCurrentCompiler(Class<?> clazz) {
    final ClassloaderByteArraySource source = ClassloaderByteArraySource.fromContext();
    final Sample s = new Sample();
    s.className = ClassName.fromClass(clazz);
    s.clazz = ClassTree.fromBytes(source.getBytes(clazz.getName()).get());
    s.compiler = "current";
    return s;
  }

  public void assertFiltersMutationsFromMutator(String id, Class<?> clazz) {
    final Sample s = sampleForClass(clazz);
    final GregorMutater mutator = mutateFromClassLoader();
    final List<MutationDetails> mutations = mutator.findMutations(s.className);
    final Collection<MutationDetails> actual = filter(s.clazz, mutations, mutator);

    final SoftAssertions softly = new SoftAssertions();
    checkHasNMutants(1, s, softly, mutations);

    final List<MutationDetails> filteredOut = FCollection.filter(mutations, notIn(actual));

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

  private Predicate<MutationDetails> notIn(
      final Collection<MutationDetails> actual) {
    return a -> !actual.contains(a);
  }

  private void assertFiltersNMutants(int n, GregorMutater mutator, Sample s, SoftAssertions softly) {
    final List<MutationDetails> mutations = mutator.findMutations(s.className);
    final Collection<MutationDetails> actual = filter(s.clazz, mutations, mutator);

    checkHasNMutants(n, s, softly, mutations);

    softly.assertThat((mutations.size() == 0) && (n == 0))
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
    return new GregorMutater(this.source, m -> true, this.mutators);
  }

  private GregorMutater mutateFromClassLoader() {
    return new GregorMutater( ClassloaderByteArraySource.fromContext(), m -> true, this.mutators);
  }


  private String makeClassName(String sample, String compiler) {
    final String clazz = MessageFormat.format(this.path, sample, compiler);
    return clazz;
  }


  private List<Sample> samples(final String sample) {
    final Function<String, Stream<Sample>> toPair = compiler -> {
      final String clazz = makeClassName(sample, compiler);
      final Optional<byte[]> bs = FilterTester.this.source.getBytes(clazz);
      if (bs.isPresent()) {
        final Sample p = new Sample();
        p.className = ClassName.fromString(clazz);
        p.clazz = ClassTree.fromBytes(bs.get());
        p.compiler = compiler;
        return Stream.of(p);
      }
      return Stream.empty();

    };
    return COMPILERS.stream().flatMap(toPair).collect(Collectors.toList());
  }

  private boolean atLeastOneSampleExists(String sample) {
    for (final String compiler : COMPILERS) {
      final String clazz = makeClassName(sample, compiler);
      if (this.source.getBytes(clazz).isPresent()) {
        return true;
      }
    }
    throw new RuntimeException("No samples found for any compiler for " + sample);
  }

  private Collection<MutationDetails> filter(ClassTree clazz,
      List<MutationDetails> mutations, Mutater mutator) {
    this.testee.begin(clazz);
    final Collection<MutationDetails> actual = this.testee.intercept(mutations, mutator);
    this.testee.end();
    return actual;
  }


  private Sample sampleForClass(Class<?> clazz) {
    final ClassloaderByteArraySource source = ClassloaderByteArraySource.fromContext();
    final Sample s = new Sample();
    s.className = ClassName.fromClass(clazz);
    s.clazz = ClassTree.fromBytes(source.getBytes(clazz.getName()).get());
    s.compiler = "current";
    return s;
  }
}

class Sample {
  ClassName className;
  String compiler;
  ClassTree clazz;
}
