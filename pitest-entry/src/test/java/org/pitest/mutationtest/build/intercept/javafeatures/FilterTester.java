package org.pitest.mutationtest.build.intercept.javafeatures;

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

import static org.assertj.core.api.Assertions.assertThat;

@Deprecated
public class FilterTester {

  private static final Collection<String> COMPILERS = Arrays.asList("javac", "javac11", "ecj", "aspectj");

  private final String path;
  private final ClassByteArraySource source = new ResourceFolderByteArraySource();
  private final MutationInterceptor testee;
  private final Collection<MethodMutatorFactory> mutators;
  private final Collection<String> compilers;

  public FilterTester(String path, MutationInterceptor testee, Collection<String> compilers, MethodMutatorFactory ... mutators) {
    this(path, testee, compilers, Arrays.asList(mutators));
  }

  public FilterTester(String path, MutationInterceptor testee, Collection<MethodMutatorFactory> mutators) {
    this(path, testee, COMPILERS, mutators);
  }

  public FilterTester(String path, MutationInterceptor testee, MethodMutatorFactory ... mutators) {
    this(path, testee, COMPILERS, Arrays.asList(mutators));
  }

  public FilterTester(String path, MutationInterceptor testee, Collection<String> compilers, Collection<MethodMutatorFactory> mutators) {
    this.mutators = mutators;
    this.testee = testee;
    this.path = path;
    this.compilers = compilers;
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

  public void assertCombinedMutantExists(Predicate<MutationDetails> match, Class<?> clazz) {
    Sample s = makeSampleForCurrentCompiler(clazz);
    GregorMutater mutator = mutateFromClassLoader();
    List<MutationDetails> mutations = mutator.findMutations(s.className);
    Collection<MutationDetails> actual = filter(s.clazz, mutations, mutator);

    assertThat(actual).anyMatch(match.and( m -> m.getId().getIndexes().size() > 1));
  }

  public void assertLeavesNMutants(int n, Class<?> clazz) {
    final Sample s = makeSampleForCurrentCompiler(clazz);
    GregorMutater mutator = mutateFromClassLoader();
    final List<MutationDetails> mutations = mutator.findMutations(s.className);
    final Collection<MutationDetails> actual = filter(s.clazz, mutations, mutator);

    assertThat(actual)
            .describedAs("Wrong number of mutants with " + s.compiler + " for class \n" + s.clazz + " (started with " + mutations.size() + ")")
            .hasSize(n);
  }

  public void assertLeavesNMutants(int n, String sample) {
    final GregorMutater mutator = mutateFromResourceDir();
    atLeastOneSampleExists(sample);

    final SoftAssertions softly = new SoftAssertions();

    for (final Sample s : samples(sample)) {
      final List<MutationDetails> mutations = mutator.findMutations(s.className);
      final Collection<MutationDetails> actual = filter(s.clazz, mutations, mutator);

      softly.assertThat(actual)
      .describedAs("Wrong number of mutants with " + s.compiler + " for class \n" + s.clazz + " (started with " + mutations.size() + ")")
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


  public void assertFiltersNoMutationsMatching(Predicate<MutationDetails> match, Class<?> clazz) {
    final Sample s = makeSampleForCurrentCompiler(clazz);

    final SoftAssertions softly = new SoftAssertions();
    GregorMutater mutator = mutateFromClassLoader();

    assertFiltersNoMatchingMutants(match, mutator, s, softly);

    softly.assertAll();
  }

  public void assertFiltersNoMutationsMatching(Predicate<MutationDetails> match, String sample) {
    final GregorMutater mutator = mutateFromResourceDir();
    final SoftAssertions softly = new SoftAssertions();

    for (final Sample s : samples(sample)) {
      assertFiltersNoMatchingMutants(match, mutator, s, softly);
    }

    softly.assertAll();
  }

  public void assertFiltersMutationsMatching(Predicate<MutationDetails> match, Class<?> clazz) {
    final Sample s = makeSampleForCurrentCompiler(clazz);

    final SoftAssertions softly = new SoftAssertions();
    GregorMutater mutator = mutateFromClassLoader();

    assertFiltersMatchingMutants(match, mutator, s, softly);

    softly.assertAll();
  }

  public void assertFiltersMutationsMatching(Predicate<MutationDetails> match, String sample) {
    final GregorMutater mutator = mutateFromResourceDir();
    atLeastOneSampleExists(sample);

    final SoftAssertions softly = new SoftAssertions();

    for (final Sample s : samples(sample)) {
      assertFiltersMatchingMutants(match, mutator, s, softly);
    }

    softly.assertAll();
  }

  private void assertFiltersMatchingMutants(Predicate<MutationDetails> match, GregorMutater mutator, Sample s, SoftAssertions softly) {
    final List<MutationDetails> mutations = mutator.findMutations(s.className);
    final Collection<MutationDetails> actual = filter(s.clazz, mutations, mutator);

    checkHasMutantsMatching(match, s, softly, mutations);

    softly.assertThat(actual)
            .describedAs("Expected to filter out all matching mutants")
            .noneMatch(match);
  }

  private void assertFiltersNoMatchingMutants(Predicate<MutationDetails> match, GregorMutater mutator, Sample s, SoftAssertions softly) {
    final List<MutationDetails> mutations = mutator.findMutations(s.className);
    final Collection<MutationDetails> actual = filter(s.clazz, mutations, mutator);

    checkHasMutantsMatching(match, s, softly, mutations);

    softly.assertThat(actual)
            .describedAs("Expected to filter no matching mutants")
            .anyMatch(match);
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

    softly.assertThat(filteredOut).describedAs("No mutants filtered " + s).isNotEmpty();
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
                  + " " + s.clazz + " [original mutants " + describe(mutations) + "]")
    .hasSize(mutations.size() - n);
  }

  private String describe(List<MutationDetails> mutations) {
    return mutations.stream()
            .map(m -> m.getMutator())
            .collect(Collectors.joining(","));
  }

  private void checkHasNMutants(int n, Sample s, SoftAssertions softly,
      List<MutationDetails> mutations) {
    softly.assertThat(mutations.size())
    .describedAs("Fewer mutations produced than expected with " + s.compiler + ". This test has a bug in it.\n" + s.clazz)
    .isGreaterThanOrEqualTo(n);
  }

  private void checkHasMutantsMatching(Predicate<MutationDetails> match, Sample s, SoftAssertions softly,
                                List<MutationDetails> mutations) {
    softly.assertThat(mutations)
            .describedAs("No matching mutations produced with " + s.compiler + " compiler. This test has a bug in it.\n" + s.clazz)
            .anyMatch(match);
  }
  private GregorMutater mutateFromResourceDir() {
    return new GregorMutater(this.source, m -> true, this.mutators);
  }

  private GregorMutater mutateFromClassLoader() {
    return new GregorMutater( ClassloaderByteArraySource.fromContext(), m -> true, this.mutators);
  }


  private String makeClassName(String sample, String compiler) {
    return MessageFormat.format(this.path, sample, compiler);
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
    return compilers.stream().flatMap(toPair).collect(Collectors.toList());
  }

  private boolean atLeastOneSampleExists(String sample) {
    for (final String compiler : compilers) {
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

  @Override
  public String toString() {
    return "Compiled by " + compiler + "\n" + clazz.toString();
  }
}
