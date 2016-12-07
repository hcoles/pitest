package org.pitest.mutationtest.incremental;

import java.math.BigInteger;
import java.util.Map;

import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassInfoSource;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.CodeSource;
import org.pitest.functional.Option;
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
  public Option<MutationStatusTestPair> getPreviousResult(
      final MutationIdentifier id) {
    return Option.some(this.previousResults.get(id));
  }

  @Override
  public boolean hasClassChanged(final ClassName className) {
    final ClassHistory historic = this.previousClassPath.get(className);
    if (historic == null) {
      return true;
    }

    final Option<ClassInfo> current = this.code.fetchClass(className);
    return !current.value().getHierarchicalId().equals(historic.getId());

  }

  @Override
  public boolean hasCoverageChanged(final ClassName className,
      final BigInteger currentCoverage) {
    return !this.previousClassPath.get(className).getCoverageId()
        .equals(currentCoverage.toString(16));
  }

}
