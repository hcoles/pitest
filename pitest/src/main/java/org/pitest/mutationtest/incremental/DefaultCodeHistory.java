package org.pitest.mutationtest.incremental;

import java.util.Map;

import org.pitest.classinfo.ClassIdentifier;
import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassInfoSource;
import org.pitest.classinfo.ClassName;
import org.pitest.functional.Option;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.execute.MutationStatusTestPair;

public class DefaultCodeHistory implements CodeHistory {

  private final ClassInfoSource                                 code;
  private final Map<MutationIdentifier, MutationStatusTestPair> previousResults;
  private final Map<ClassName, ClassIdentifier>                 previousClassPath;

  public DefaultCodeHistory(final ClassInfoSource code,
      final Map<MutationIdentifier, MutationStatusTestPair> previousResults,
      final Map<ClassName, ClassIdentifier> previousClassPath) {
    this.code = code;
    this.previousResults = previousResults;
    this.previousClassPath = previousClassPath;
  }

  public Option<MutationStatusTestPair> getPreviousResult(
      final MutationIdentifier id) {
    return Option.some(this.previousResults.get(id));
  }

  public boolean hasClassChanged(final ClassName className) {
    final ClassIdentifier historic = this.previousClassPath.get(className);
    if (historic == null) {
      return true;
    }

    final Option<ClassInfo> current = this.code.fetchClass(className);
    if (!current.value().getId().equals(historic)) {
      return true;
    }

    return false;
  }

}
