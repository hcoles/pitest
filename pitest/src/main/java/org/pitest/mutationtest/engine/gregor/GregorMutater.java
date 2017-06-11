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

import static org.pitest.functional.prelude.Prelude.and;
import static org.pitest.functional.prelude.Prelude.not;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.pitest.bytecode.FrameOptions;
import org.pitest.bytecode.NullVisitor;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassName;
import org.pitest.classinfo.ComputeClassWriter;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.Option;
import org.pitest.functional.predicate.Predicate;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;

public class GregorMutater implements Mutater {

  private final Map<String, String>       computeCache   = new HashMap<String, String>();
  private final Predicate<MethodInfo>     filter;
  private final ClassByteArraySource      byteSource;
  private final Set<MethodMutatorFactory> mutators       = new HashSet<MethodMutatorFactory>();

  public GregorMutater(final ClassByteArraySource byteSource,
      final Predicate<MethodInfo> filter,
      final Collection<MethodMutatorFactory> mutators) {
    this.filter = filter;
    this.mutators.addAll(mutators);
    this.byteSource = byteSource;
  }

  @Override
  public FunctionalList<MutationDetails> findMutations(
      final ClassName classToMutate) {

    final ClassContext context = new ClassContext();
    context.setTargetMutation(Option.<MutationIdentifier> none());
    return GregorMutater.this.byteSource.getBytes(
        classToMutate.asInternalName()).flatMap(findMutations(context));

  }

  private F<byte[], Iterable<MutationDetails>> findMutations(
      final ClassContext context) {
    return new F<byte[], Iterable<MutationDetails>>() {
      @Override
      public Iterable<MutationDetails> apply(final byte[] bytes) {
        return findMutationsForBytes(context, bytes);
      }
    };
  }

  private Collection<MutationDetails> findMutationsForBytes(
      final ClassContext context, final byte[] classToMutate) {

    final ClassReader first = new ClassReader(classToMutate);
    final NullVisitor nv = new NullVisitor();
    final MutatingClassVisitor mca = new MutatingClassVisitor(nv, context,
        filterMethods(), this.mutators);

    first.accept(mca, ClassReader.EXPAND_FRAMES);

    return context.getCollectedMutations();
  }

  @Override
  public Mutant getMutation(final MutationIdentifier id) {

    final ClassContext context = new ClassContext();
    context.setTargetMutation(Option.some(id));

    final Option<byte[]> bytes = this.byteSource.getBytes(id.getClassName()
        .asJavaName());

    final ClassReader reader = new ClassReader(bytes.value());
    final ClassWriter w = new ComputeClassWriter(this.byteSource,
        this.computeCache, FrameOptions.pickFlags(bytes.value()));
    final MutatingClassVisitor mca = new MutatingClassVisitor(w, context,
        filterMethods(), FCollection.filter(this.mutators,
            isMutatorFor(id)));
    reader.accept(mca, ClassReader.EXPAND_FRAMES);

    final List<MutationDetails> details = context.getMutationDetails(context
        .getTargetMutation().value());

    return new Mutant(details.get(0), w.toByteArray());

  }

  private static Predicate<MethodMutatorFactory> isMutatorFor(
      final MutationIdentifier id) {
    return new Predicate<MethodMutatorFactory>() {

      @Override
      public Boolean apply(final MethodMutatorFactory a) {
        return id.getMutator().equals(a.getGloballyUniqueId());
      }

    };
  }

  @SuppressWarnings("unchecked")
  private Predicate<MethodInfo> filterMethods() {
    return and(this.filter, filterSyntheticMethods(),
        not(isGeneratedEnumMethod()), not(isGroovyClass()));
  }

  private static F<MethodInfo, Boolean> isGroovyClass() {
    return new Predicate<MethodInfo>() {
      @Override
      public Boolean apply(final MethodInfo a) {
        return a.isInGroovyClass();
      }

    };
  }

  private static Predicate<MethodInfo> filterSyntheticMethods() {
    return new Predicate<MethodInfo>() {

      @Override
      public Boolean apply(final MethodInfo a) {
        // filter out synthetic methods,
        // except lambda$... methods, which contain code from lambda expressions
        return !a.isSynthetic() || a.getName().startsWith("lambda$");
      }

    };
  }

  private static Predicate<MethodInfo> isGeneratedEnumMethod() {
    return new Predicate<MethodInfo>() {
      @Override
      public Boolean apply(final MethodInfo a) {
        return a.isGeneratedEnumMethod();
      }
    };
  }

}
