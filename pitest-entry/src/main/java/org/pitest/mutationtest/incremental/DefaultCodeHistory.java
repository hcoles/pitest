package org.pitest.mutationtest.incremental;

import java.math.BigInteger;
import java.util.Map;

import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassInfoSource;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.CodeSource;
import java.util.Optional;
import org.pitest.mutationtest.ClassHistory;
import org.pitest.mutationtest.HistoryStore;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.MutationIdentifier;

public class DefaultCodeHistory implements CodeHistory {

  private final ClassInfoSource                                 code;
  private final Map<MutationIdentifier, MutationStatusTestPair> previousResults;
  private final Map<ClassName, ClassHistory>                    previousClassPath;

  public DefaultCodeHistory(final CodeSource code,
      final HistoryStore historyStore) {
    this(code, historyStore.getHistoricResults(), historyStore
        .getHistoricClassPath());
  }

  public DefaultCodeHistory(final ClassInfoSource code,
      final Map<MutationIdentifier, MutationStatusTestPair> previousResults,
      final Map<ClassName, ClassHistory> previousClassPath) {
    this.code = code;
    this.previousResults = previousResults;
    this.previousClassPath = previousClassPath;
  }

  @Override
  public Optional<MutationStatusTestPair> getPreviousResult(
      final MutationIdentifier id) {
    return Optional.ofNullable(this.previousResults.get(id));
  }

  @Override
  public boolean hasClassChanged(final ClassName className) {
    final ClassHistory historic = this.previousClassPath.get(className);
    if (historic == null) {
      return true;
    }

    final Optional<ClassInfo> current = this.code.fetchClass(className);
    return !current.get().getHierarchicalId().equals(historic.getId());

  }

  @Override
  public boolean hasCoverageChanged(final ClassName className,
      final BigInteger currentCoverage) {
    return !this.previousClassPath.get(className).getCoverageId()
        .equals(currentCoverage.toString(16));
  }

}
