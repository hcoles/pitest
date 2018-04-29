package org.pitest.mutationtest;

import java.util.ArrayList;
import java.util.Collection;

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
    final int prime = 31;
    int result = 1;
    result = (prime * result)
        + ((this.mutations == null) ? 0 : this.mutations.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ClassMutationResults other = (ClassMutationResults) obj;
    if (this.mutations == null) {
      if (other.mutations != null) {
        return false;
      }
    } else if (!this.mutations.equals(other.mutations)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "ClassMutationResults [mutations=" + this.mutations + "]";
  }

}
