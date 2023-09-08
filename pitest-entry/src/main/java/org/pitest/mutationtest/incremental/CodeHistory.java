package org.pitest.mutationtest.incremental;

import java.math.BigInteger;
import java.util.Map;

import org.pitest.classinfo.ClassHash;
import org.pitest.classinfo.ClassHashSource;
import org.pitest.classinfo.ClassName;
import java.util.Optional;
import org.pitest.mutationtest.ClassHistory;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.MutationIdentifier;

class CodeHistory {

  private final ClassHashSource code;
  private final Map<MutationIdentifier, MutationStatusTestPair> previousResults;
  private final Map<ClassName, ClassHistory>                    previousClassPath;

  CodeHistory(final ClassHashSource code,
      final Map<MutationIdentifier, MutationStatusTestPair> previousResults,
      final Map<ClassName, ClassHistory> previousClassPath) {
    this.code = code;
    this.previousResults = previousResults;
    this.previousClassPath = previousClassPath;
  }

  public Optional<MutationStatusTestPair> getPreviousResult(
      final MutationIdentifier id) {
    return Optional.ofNullable(this.previousResults.get(id));
  }

  public boolean hasClassChanged(final ClassName className) {
    final ClassHistory historic = this.previousClassPath.get(className);
    if (historic == null) {
      return true;
    }

    final Optional<ClassHash> current = this.code.fetchClassHash(className);
    return !current.get().getHierarchicalId().equals(historic.getId());

  }

  public boolean hasCoverageChanged(final ClassName className,
      final BigInteger currentCoverage) {
    return !this.previousClassPath.get(className).getCoverageId()
        .equals(currentCoverage.toString(16));
  }

}
