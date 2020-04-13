package org.pitest.mutationtest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import org.pitest.classinfo.ClassName;

/**
 * Details of mutation results from a single class.
 *
 * A single instance will only ever contain mutations relating to a single
 * class. The mutations for a class may however be spread across multiple
 * instances.
 */
public class ClassMutationResults {

  private final Collection<MutationResult> mutations = new ArrayList<>();

  public ClassMutationResults(final Collection<MutationResult> mutations) {
    this.mutations.addAll(mutations);
  }

  public String getFileName() {
    return this.mutations.iterator().next().getDetails().getFilename();
  }

  public Collection<MutationResult> getMutations() {
    return this.mutations;
  }

  public ClassName getMutatedClass() {
    return this.mutations.iterator().next().getDetails().getClassName();
  }

  public String getPackageName() {
    final ClassName name = getMutatedClass();
    final int lastDot = name.asJavaName().lastIndexOf('.');
    return lastDot > 0 ? name.asJavaName().substring(0, lastDot) : "default";
  }

  @Override
  public int hashCode() {
    return Objects.hash(mutations);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final ClassMutationResults other = (ClassMutationResults) obj;
    return Objects.equals(mutations, other.mutations);
  }

  @Override
  public String toString() {
    return "ClassMutationResults [mutations=" + this.mutations + "]";
  }

}
