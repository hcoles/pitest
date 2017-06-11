/*
 * Copyright 2010 Henry Coles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest.mutationtest.engine.gregor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassPathByteArraySource;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.predicate.Predicate;
import org.pitest.functional.predicate.True;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.simpletest.ExcludedPrefixIsolationStrategy;
import org.pitest.simpletest.Transformation;
import org.pitest.simpletest.TransformingClassLoader;
import org.pitest.util.IsolationUtils;
import org.pitest.util.Unchecked;

public abstract class MutatorTestBase {

  protected GregorMutater engine;

  protected FunctionalList<MutationDetails> findMutationsFor(
      final Class<?> clazz) {
    return this.engine.findMutations(ClassName.fromClass(clazz));
  }

  protected FunctionalList<MutationDetails> findMutationsFor(final String clazz) {
    return this.engine.findMutations(ClassName.fromString(clazz));
  }

  protected void createTesteeWith(final Predicate<MethodInfo> filter,
      final MethodMutatorFactory... mutators) {
    this.engine = new GregorMutater(new ClassPathByteArraySource(), filter,
        Arrays.asList(mutators));
  }

  protected void createTesteeWith(final ClassByteArraySource source,
      final Predicate<MethodInfo> filter,
      final Collection<MethodMutatorFactory> mutators) {
    this.engine = new GregorMutater(source, filter, mutators);
  }

  protected void createTesteeWith(final Predicate<MethodInfo> filter,
      final Collection<MethodMutatorFactory> mutators) {
    createTesteeWith(new ClassPathByteArraySource(), filter, mutators);
  }

  protected void createTesteeWith(final Predicate<MethodInfo> filter,
      final Collection<String> loggingClasses,
      final Collection<MethodMutatorFactory> mutators) {
    this.engine = new GregorMutater(new ClassPathByteArraySource(), filter,
        mutators);
  }

  protected void createTesteeWith(
      final Collection<MethodMutatorFactory> mutators) {
    createTesteeWith(True.<MethodInfo> all(), mutators);
  }

  protected void createTesteeWith(final MethodMutatorFactory... mutators) {
    createTesteeWith(True.<MethodInfo> all(), mutators);
  }

  protected <T> void assertMutantCallableReturns(final Callable<T> unmutated,
      final Mutant mutant, final T expected) throws Exception {
    assertEquals(expected, mutateAndCall(unmutated, mutant));
  }

  protected void assertNoMutants(final Class<?> mutee) {
    final Collection<MutationDetails> actual = findMutationsFor(mutee);
    assertTrue(actual.isEmpty());
  }

  protected <T> T mutateAndCall(final Callable<T> unmutated, final Mutant mutant) {
    try {
      final ClassLoader loader = createClassLoader(mutant);
      return runInClassLoader(loader, unmutated);
    } catch (final RuntimeException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw Unchecked.translateCheckedException(ex);
    }
  }

  private ClassLoader createClassLoader(final Mutant mutant) throws Exception {
    final TransformingClassLoader loader = new TransformingClassLoader(
        createTransformation(mutant), new ExcludedPrefixIsolationStrategy());

    return loader;
  }

  private Transformation createTransformation(final Mutant mutant) {
    return new Transformation() {

      @Override
      public byte[] transform(final String name, final byte[] bytes) {
        if (name.equals(mutant.getDetails().getClassName().asJavaName())) {
          return mutant.getBytes();
        } else {
          return bytes;
        }
      }

    };
  }

  @SuppressWarnings("unchecked")
  private <T> T runInClassLoader(final ClassLoader loader,
      final Callable<T> callable) throws Exception {
    final Callable<T> c = (Callable<T>) IsolationUtils.cloneForLoader(callable,
        loader);
    return c.call();

  }

  protected List<Mutant> getMutants(
      final FunctionalList<MutationDetails> details) {
    return details.map(createMutant());
  }

  private F<MutationDetails, Mutant> createMutant() {
    return new F<MutationDetails, Mutant>() {

      @Override
      public Mutant apply(final MutationDetails a) {
        return MutatorTestBase.this.engine.getMutation(a.getId());
      }

    };
  }

  protected Mutant getFirstMutant(final Collection<MutationDetails> actual) {
    assertFalse("No mutant found", actual.isEmpty());
    final Mutant mutant = this.engine.getMutation(actual.iterator().next()
        .getId());
    verifyMutant(mutant);
    return mutant;
  }

  protected Mutant getFirstMutant(final Class<?> mutee) {
    final Collection<MutationDetails> actual = findMutationsFor(mutee);
    return getFirstMutant(actual);
  }

  private void verifyMutant(final Mutant mutant) {
    // printMutant(mutant);
    final StringWriter sw = new StringWriter();
    final PrintWriter pw = new PrintWriter(sw);
    CheckClassAdapter.verify(new ClassReader(mutant.getBytes()), false, pw);
    assertTrue(sw.toString(), sw.toString().length() == 0);

  }

  protected void printMutant(final Mutant mutant) {    
     final ClassReader reader = new ClassReader(mutant.getBytes());
     reader.accept(new TraceClassVisitor(null, new ASMifier(), new PrintWriter(
         System.out)), ClassReader.EXPAND_FRAMES);
  }

  protected void assertMutantsReturn(final Callable<String> mutee,

      final FunctionalList<MutationDetails> details,
      final String... expectedResults) {

    final List<Mutant> mutants = this.getMutants(details);
    assertEquals("Should return one mutant for each request", details.size(),
        mutants.size());
    final FunctionalList<String> results = FCollection.map(mutants,
        mutantToStringReults(mutee));

    int i = 0;
    for (final String actual : results) {
      assertEquals(expectedResults[i], actual);
      i++;
    }
  }

  private F<Mutant, String> mutantToStringReults(final Callable<String> mutee) {
    return new F<Mutant, String>() {

      @Override
      public String apply(final Mutant mutant) {
        return mutateAndCall(mutee, mutant);
      }

    };
  }

  protected void assertMutantsAreFrom(
      final FunctionalList<MutationDetails> actualDetails,
      final Class<?>... mutators) {
    assertEquals(mutators.length, actualDetails.size());
    int i = 0;
    for (final MutationDetails each : actualDetails) {
      assertEquals(each.getId().getMutator(), mutators[i].getName());
      i++;
    }
  }

  protected Mutant createFirstMutant(
      final Class<? extends Callable<String>> mutee) {
    final Collection<MutationDetails> actual = findMutationsFor(mutee);
    return getFirstMutant(actual);
  }

  protected Predicate<MethodInfo> mutateOnlyCallMethod() {
    return new Predicate<MethodInfo>() {

      @Override
      public Boolean apply(final MethodInfo a) {
        return a.getName().equals("call");
      }

    };
  }

  protected F<MutationDetails, Boolean> descriptionContaining(final String value) {
    return new F<MutationDetails, Boolean>() {
      @Override
      public Boolean apply(final MutationDetails a) {
        return a.getDescription().contains(value);
      }
    };
  }
}
