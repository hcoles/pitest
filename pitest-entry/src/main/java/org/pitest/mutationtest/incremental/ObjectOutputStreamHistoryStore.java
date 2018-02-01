package org.pitest.mutationtest.incremental;

import org.apache.commons.codec.binary.Base64;
import org.pitest.classinfo.ClassName;
import org.pitest.classinfo.HierarchicalClassId;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.functional.Option;
import org.pitest.mutationtest.ClassHistory;
import org.pitest.mutationtest.HistoryStore;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.util.Log;
import org.pitest.util.Unchecked;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class ObjectOutputStreamHistoryStore implements HistoryStore {

  private static final Logger                                   LOG               = Log
      .getLogger();
  private final WriterFactory                                   outputFactory;
  private final BufferedReader                                  input;
  private final Map<MutationIdentifier, MutationStatusTestPair> previousResults   = new HashMap<>();
  private final Map<ClassName, ClassHistory>                    previousClassPath = new HashMap<>();

  public ObjectOutputStreamHistoryStore(final WriterFactory output,
      final Option<Reader> input) {
    this.outputFactory = output;
    this.input = createReader(input);
  }

  private BufferedReader createReader(Option<Reader> input) {
    if (input.hasSome()) {
      return new BufferedReader(input.value());
    }
    return null;
  }

  @Override
  public void recordClassPath(final Collection<HierarchicalClassId> ids,
      final CoverageDatabase coverageInfo) {
    final PrintWriter output = this.outputFactory.create();
    output.println(ids.size());
    for (final HierarchicalClassId each : ids) {
      final ClassHistory coverage = new ClassHistory(each,
          coverageInfo.getCoverageIdForClass(each.getName()).toString(16));
      output.println(serialize(coverage));
    }
    output.flush();
  }

  @Override
  public void recordResult(final MutationResult result) {
    final PrintWriter output = this.outputFactory.create();
    output.println(serialize(new ObjectOutputStreamHistoryStore.IdResult(
        result.getDetails().getId(), result.getStatusTestPair())));
    output.flush();
  }

  @Override
  public Map<MutationIdentifier, MutationStatusTestPair> getHistoricResults() {
    return this.previousResults;
  }

  @Override
  public Map<ClassName, ClassHistory> getHistoricClassPath() {
    return this.previousClassPath;
  }

  @Override
  public void initialize() {
    if (this.input != null) {
      restoreClassPath();
      restoreResults();
      try {
        this.input.close();
      } catch (final IOException e) {
        throw Unchecked.translateCheckedException(e);
      }
    }
  }

  private void restoreResults() {
    String line;
    try {
      line = this.input.readLine();
      while (line != null) {
        final IdResult result = deserialize(line, IdResult.class);
        this.previousResults.put(result.id, result.status);
        line = this.input.readLine();
      }
    } catch (final IOException e) {
      LOG.warning("Could not read previous results");
    }
  }

  private void restoreClassPath() {
    try {
      final long classPathSize = Long.valueOf(this.input.readLine());
      for (int i = 0; i != classPathSize; i++) {
        final ClassHistory coverage = deserialize(this.input.readLine(),
            ClassHistory.class);
        this.previousClassPath.put(coverage.getName(), coverage);
      }
    } catch (final IOException e) {
      LOG.warning("Could not read previous classpath");
    }
  }

  private <T> T deserialize(String string, Class<T> clazz) throws IOException {
    try {
      final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
          Base64.decodeBase64(string));
      ObjectInputStream objectInputStream = new ObjectInputStream(
          byteArrayInputStream);
      return clazz.cast(objectInputStream.readObject());
    } catch (ClassNotFoundException e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

  private <T> String serialize(T t) {
    try {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      ObjectOutputStream objectOutputStream = new ObjectOutputStream(
          byteArrayOutputStream);
      objectOutputStream.writeObject(t);
      return Base64.encodeBase64String(byteArrayOutputStream.toByteArray());
    } catch (IOException e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

  private static class IdResult implements Serializable {
    private static final long    serialVersionUID = 1L;
    final MutationIdentifier     id;
    final MutationStatusTestPair status;

    IdResult(final MutationIdentifier id, final MutationStatusTestPair status) {
      this.id = id;
      this.status = status;
    }

  }

}