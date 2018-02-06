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
package org.pitest.dependency;

import static org.pitest.functional.prelude.Prelude.and;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.logging.Logger;

import org.objectweb.asm.ClassReader;
import org.pitest.bytecode.NullVisitor;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.functional.FCollection;
import java.util.Optional;
import org.pitest.functional.SideEffect1;
import org.pitest.util.Functions;
import org.pitest.util.Log;

public class DependencyExtractor {
  private static final Logger        LOG = Log.getLogger();
  private final int                  depth;
  private final ClassByteArraySource classToBytes;

  public DependencyExtractor(final ClassByteArraySource classToBytes,
      final int depth) {
    this.depth = depth;
    this.classToBytes = classToBytes;
  }

  public Collection<String> extractCallDependenciesForPackages(
      final String clazz, final Predicate<String> targetPackages)
          throws IOException {
    final Set<String> allDependencies = extractCallDependencies(clazz,
        new IgnoreCoreClasses());
    return FCollection.filter(allDependencies,
        and(asJVMNamePredicate(targetPackages), notSuppliedClass(clazz)));
  }

  private static Predicate<String> notSuppliedClass(final String clazz) {
    return a -> !Functions.jvmClassToClassName().apply(a).equals(clazz);
  }

  private static Predicate<String> asJVMNamePredicate(
      final Predicate<String> predicate) {
    return a -> predicate.test(Functions.jvmClassToClassName().apply(a));
  }

  public Collection<String> extractCallDependenciesForPackages(
      final String clazz, final Predicate<String> targetPackages,
      final Predicate<DependencyAccess> doNotTraverse) throws IOException {
    final Set<String> allDependencies = extractCallDependencies(clazz,
        doNotTraverse);
    return FCollection.filter(allDependencies, targetPackages);
  }

  Set<String> extractCallDependencies(final String clazz,
      final Predicate<DependencyAccess> filter) throws IOException {

    return this
        .extractCallDependencies(clazz, new TreeSet<String>(), filter, 0);
  }

  public int getMaxDistance() {
    return this.depth;
  }

  private Set<String> extractCallDependencies(final String clazz,
      final TreeSet<String> visited, final Predicate<DependencyAccess> filter,
      final int currentDepth) throws IOException {

    final Map<String, List<DependencyAccess>> classesToAccesses = groupDependenciesByClass(extractRelevantDependencies(
        clazz, filter));
    final Set<String> dependencies = new HashSet<>(
        classesToAccesses.keySet());

    dependencies.removeAll(visited);
    visited.addAll(dependencies);

    if ((currentDepth < (this.depth - 1)) || (this.depth == 0)) {

      dependencies.addAll(examineChildDependencies(currentDepth, dependencies,
          visited, filter));

    }

    return dependencies;

  }

  private Set<String> examineChildDependencies(final int currentDepth,
      final Set<String> classes, final TreeSet<String> visited,
      final Predicate<DependencyAccess> filter) throws IOException {

    final Set<String> deps = new HashSet<>();
    for (final String each : classes) {
      final Set<String> childDependencies = extractCallDependencies(each,
          visited, filter, currentDepth + 1);
      deps.addAll(childDependencies);
    }
    return deps;
  }

  private Set<DependencyAccess> extractRelevantDependencies(final String clazz,
      final Predicate<DependencyAccess> filter) throws IOException {
    final List<DependencyAccess> dependencies = extract(clazz, filter);
    final Set<DependencyAccess> relevantDependencies = new TreeSet<>(
        equalDestinationComparator());
    FCollection.filter(dependencies, filter, relevantDependencies);
    return relevantDependencies;
  }

  private static Comparator<DependencyAccess> equalDestinationComparator() {
    return (o1, o2) -> o1.getDest().compareTo(o2.getDest());
  }

  private List<DependencyAccess> extract(final String clazz,
      final Predicate<DependencyAccess> filter) throws IOException {
    final Optional<byte[]> bytes = this.classToBytes.getBytes(clazz);
    if (!bytes.isPresent()) {
      LOG.warning("No bytes found for " + clazz);
      return Collections.emptyList();
    }
    final ClassReader reader = new ClassReader(bytes.get());
    final List<DependencyAccess> dependencies = new ArrayList<>();

    final SideEffect1<DependencyAccess> se = constructCollectingSideEffectForVisitor(
        dependencies, and(nameIsEqual(clazz).negate(), filter));
    final DependencyClassVisitor dcv = new DependencyClassVisitor(
        new NullVisitor(), se);
    reader.accept(dcv, ClassReader.EXPAND_FRAMES);
    return dependencies;
  }

  private Map<String, List<DependencyAccess>> groupDependenciesByClass(
      final Set<DependencyAccess> relevantDependencies) {
    final List<DependencyAccess> sortedByClass = new ArrayList<>(
        relevantDependencies.size());
    Collections.sort(sortedByClass, classNameComparator());

    return FCollection.fold(addDependenciesToMap(),
        new HashMap<String, List<DependencyAccess>>(), relevantDependencies);

  }

  private static BiFunction<Map<String, List<DependencyAccess>>, DependencyAccess, Map<String, List<DependencyAccess>>> addDependenciesToMap() {

    return (map, access) -> {

    List<DependencyAccess> list = map.get(access.getDest().getOwner());
    if (list == null) {
    list = new ArrayList<>();
    }
    list.add(access);
    map.put(access.getDest().getOwner(), list);
    return map;
   };
  }

  private static Comparator<DependencyAccess> classNameComparator() {
    return (lhs, rhs) -> lhs.getDest().getOwner().compareTo(rhs.getDest().getOwner());
  }

  private static Predicate<DependencyAccess> nameIsEqual(final String clazz) {
    return a -> a.getDest().getOwner().equals(clazz);
  }

  private static SideEffect1<DependencyAccess> constructCollectingSideEffectForVisitor(
      final List<DependencyAccess> dependencies,
      final Predicate<DependencyAccess> predicate) {
    final SideEffect1<DependencyAccess> se = a -> {
      if (predicate.test(a)) {
        dependencies.add(a);
      }
    };
    return se;
  }

}
