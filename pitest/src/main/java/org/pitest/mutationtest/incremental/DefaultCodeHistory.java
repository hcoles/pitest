package org.pitest.mutationtest.incremental;

import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassInfoSource;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.CodeSource;
import org.pitest.functional.Option;
import org.pitest.mutationtest.ClassHistory;
import org.pitest.mutationtest.HistoryStore;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.util.Log;

import java.math.BigInteger;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultCodeHistory implements CodeHistory {

  private static final Logger LOG = Log.getLogger();

  private final ClassInfoSource                                 code;
  private final Map<MutationIdentifier, MutationStatusTestPair> previousResults;
  private final Map<ClassName, ClassHistory>                    previousClassPath;

  public DefaultCodeHistory(CodeSource code, HistoryStore historyStore) {
    this(code, historyStore.getHistoricResults(),
         historyStore.getHistoricClassPath());
  }

  public DefaultCodeHistory(ClassInfoSource code,
                            Map<MutationIdentifier, MutationStatusTestPair> previousResults,
                            Map<ClassName, ClassHistory> previousClassPath) {
    this.code = code;
    this.previousResults = previousResults;
    this.previousClassPath = previousClassPath;
  }

  public Option<MutationStatusTestPair> getPreviousResult(MutationIdentifier id) {
    return Option.some(previousResults.get(id));
  }

  public boolean hasClassChanged(ClassName className) {
    ClassHistory historic = previousClassPath.get(className);
    if (historic == null) {
      return true;
    }

    Option<ClassInfo> current = code.fetchClass(className);
    boolean changed = !current.value().getHierarchicalId().equals(historic.getId());
    if (changed) {
        LOG.log(Level.WARNING,"class changed [" + className + "] : " + current.value().getId().getHash());
    }
    return changed;
  }

  public boolean hasCoverageChanged(ClassName className,
                                    BigInteger currentCoverage) {
    String coverageId = previousClassPath.get(className).getCoverageId();
    return !coverageId.equals(currentCoverage.toString(16));
  }
}
