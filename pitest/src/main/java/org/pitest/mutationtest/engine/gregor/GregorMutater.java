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

import static org.pitest.functional.Prelude.and;
import static org.pitest.functional.Prelude.not;
import static org.pitest.util.Functions.classNameToJVMClassName;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.pitest.bytecode.NullVisitor;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.Option;
import org.pitest.functional.predicate.Predicate;
import org.pitest.internal.ClassByteArraySource;
import org.pitest.mutationtest.MutationDetails;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationIdentifier;

class GregorMutater implements Mutater {

  private final Predicate<MethodInfo>     filter;
  private final ClassByteArraySource      byteSource;
  private final Set<MethodMutatorFactory> mutators       = new HashSet<MethodMutatorFactory>();
  private final Set<String>               loggingClasses = new HashSet<String>();

  public GregorMutater(final ClassByteArraySource byteSource,
      final Predicate<MethodInfo> filter,
      final Collection<MethodMutatorFactory> mutators,
      final Collection<String> loggingClasses) {
    this.filter = filter;
    this.mutators.addAll(mutators);
    this.byteSource = byteSource;
    this.loggingClasses.addAll(FCollection.map(loggingClasses,
        classNameToJVMClassName()));
  }

  public FunctionalList<MutationDetails> findMutations(
      final Collection<String> classesToMutate) {
    return FCollection.flatMap(classesToMutate, classToMutationDetails());
  }

  private F<String, Iterable<MutationDetails>> classToMutationDetails() {
    return new F<String, Iterable<MutationDetails>>() {

      public Iterable<MutationDetails> apply(final String clazz) {
        final Context context = new Context();
        context.setTargetMutation(Option.<MutationIdentifier> none());
        return GregorMutater.this.byteSource.apply(clazz).flatMap(
            findMutations(context));
      }

    };
  }

  private F<byte[], Iterable<MutationDetails>> findMutations(
      final Context context) {
    return new F<byte[], Iterable<MutationDetails>>() {
      public Iterable<MutationDetails> apply(final byte[] bytes) {
        return findMutationsForBytes(context, bytes);
      }

    };
  }

  private Collection<MutationDetails> findMutationsForBytes(
      final Context context, final byte[] classToMutate) {

    final PremutationClassInfo classInfo = performPreScan(classToMutate);

    final ClassReader first = new ClassReader(classToMutate);
    final NullVisitor nv = new NullVisitor();
    final MutatingClassAdapter mca = new MutatingClassAdapter(nv, context,
        filterMethods(context), classInfo, this.mutators);

    first.accept(mca, ClassReader.EXPAND_FRAMES);

    return context.getCollectedMutations();

  }

  private PremutationClassInfo performPreScan(final byte[] classToMutate) {
    final ClassReader reader = new ClassReader(classToMutate);

    final PreMutationAnalyser an = new PreMutationAnalyser(this.loggingClasses);
    reader.accept(an, 0);
    return an.getClassInfo();

  }

  public Mutant getMutation(final MutationIdentifier id) {

    final Context context = new Context();
    context.setTargetMutation(Option.some(id));

    final Option<byte[]> bytes = this.byteSource.apply(id.getClazz());

    final PremutationClassInfo classInfo = performPreScan(bytes.value());

    final ClassReader reader = new ClassReader(bytes.value());
    final ClassWriter w = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    final MutatingClassAdapter mca = new MutatingClassAdapter(w, context,
        filterMethods(context), classInfo, FCollection.filter(this.mutators,
            isMutatorFor(id)));
    reader.accept(mca, ClassReader.EXPAND_FRAMES);

    final FunctionalList<MutationDetails> details = context
        .getMutationDetails(context.getTargetMutation().value());

    return new Mutant(details.get(0), w.toByteArray());

  }

  private Predicate<MethodMutatorFactory> isMutatorFor(
      final MutationIdentifier id) {
    return new Predicate<MethodMutatorFactory>() {

      public Boolean apply(final MethodMutatorFactory a) {
        return id.getMutator().equals(a.getGloballyUniqueId());
      }

    };
  }

  @SuppressWarnings("unchecked")
  private Predicate<MethodInfo> filterMethods(final Context context) {
    return and(this.filter, filterSyntheticMethods(),
        not(isEnumMethod(context)));
  }

  private Predicate<MethodInfo> filterSyntheticMethods() {
    return new Predicate<MethodInfo>() {

      public Boolean apply(final MethodInfo a) {
        return !a.isSynthetic();
      }

    };
  }

  private Predicate<MethodInfo> isEnumMethod(final Context context) {
    return new Predicate<MethodInfo>() {
      public Boolean apply(final MethodInfo a) {
        return context.getClassInfo().isEnum()
            && (isValueOfMethod(a) || isValuesMethod(a)
                || a.isStaticInitializer() || isDefaultConstructor(a));
      }

      private boolean isDefaultConstructor(final MethodInfo a) {
        return a.isConstructor() && (context.getLineNumber() == 1);
      }

      private boolean isValuesMethod(final MethodInfo a) {
        return a.getName().equals("values") && a.takesNoParameters()
            && a.isStatic();
      }

      private boolean isValueOfMethod(final MethodInfo a) {
        return a.getName().equals("valueOf")
            && a.getDesc().startsWith("(Ljava/lang/String;)") && a.isStatic();
      }

    };
  }

  public Mutant getUnmodifiedClass(final String clazz) {
    final MutationDetails details = new MutationDetails(
        MutationIdentifier.unmutated(clazz), "", "unmutated", "none", -1);
    return new Mutant(details, this.byteSource.apply(clazz).value());
  }

  public Set<MethodMutatorFactory> getMutators() {
    return this.mutators;
  }

  @Override
  public String toString() {
    return "GregorMutater [filter=" + this.filter + ", byteSource="
        + this.byteSource + ", mutators=" + this.mutators + ", loggingClasses="
        + this.loggingClasses + "]";
  }

}
