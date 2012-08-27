package org.pitest.mutationtest.incremental;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.pitest.classinfo.ClassIdentifier;
import org.pitest.classinfo.ClassName;
import org.pitest.functional.Option;
import org.pitest.internal.IsolationUtils;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.execute.MutationStatusTestPair;
import org.pitest.mutationtest.results.MutationResult;
import org.pitest.util.Unchecked;

public class XStreamHistoryStore implements HistoryStore {

  private final PrintWriter                                     output;
  private final BufferedReader                                  input;
  private final Map<MutationIdentifier, MutationStatusTestPair> previousResults   = new HashMap<MutationIdentifier, MutationStatusTestPair>();
  private final Map<ClassName, ClassIdentifier>                 previousClassPath = new HashMap<ClassName, ClassIdentifier>();

  public XStreamHistoryStore(final Writer output, final Option<Reader> input) {
    this.output = new PrintWriter(output);
    this.input = createReader(input);
  }

  private BufferedReader createReader(final Option<Reader> input) {
    if (input.hasSome()) {
      return new BufferedReader(input.value());
    }
    return null;
  }

  public void recordClassPath(final Collection<ClassIdentifier> ids) {
    this.output.println(ids.size());
    for (final ClassIdentifier each : ids) {
      this.output.println(IsolationUtils.toXml(each).replaceAll("\n", ""));
    }
    this.output.flush();

  }

  public void recordResult(final MutationResult result) {
    this.output.println(IsolationUtils.toXml(result).replaceAll("\n", ""));
  }

  public Map<MutationIdentifier, MutationStatusTestPair> getHistoricResults() {
    return this.previousResults;
  }

  public Map<ClassName, ClassIdentifier> getHistoricClassPath() {
    return this.previousClassPath;
  }

  public void initialize() {
    if (this.input != null) {
      restoreClassPath();
      restoreResults();
    }
  }

  private void restoreResults() {
    String line;
    try {
      line = this.input.readLine();
      while (line != null) {
        final MutationResult mr = (MutationResult) IsolationUtils.fromXml(line);
        this.previousResults.put(mr.getDetails().getId(),
            mr.getStatusTestPair());
        line = this.input.readLine();
      }
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    }

  }

  private void restoreClassPath() {
    try {
      final long classPathSize = Long.valueOf(this.input.readLine());
      for (int i = 0; i != classPathSize; i++) {
        final ClassIdentifier ci = (ClassIdentifier) IsolationUtils
            .fromXml(this.input.readLine());
        this.previousClassPath.put(ci.getName(), ci);
      }
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

}
