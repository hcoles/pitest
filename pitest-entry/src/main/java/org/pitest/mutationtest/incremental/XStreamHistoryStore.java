package org.pitest.mutationtest.incremental;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.pitest.classinfo.ClassIdentifier;
import org.pitest.classinfo.ClassName;
import org.pitest.classinfo.HierarchicalClassId;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.functional.Option;
import org.pitest.mutationtest.ClassHistory;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.HistoryStore;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.util.Log;
import org.pitest.util.Unchecked;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.StreamException;
import com.thoughtworks.xstream.io.xml.CompactWriter;

public class XStreamHistoryStore implements HistoryStore {

  private static final Logger                                   LOG               = Log
      .getLogger();

  private static final XStream                                  XSTREAM_INSTANCE  = configureXStream();

  private final WriterFactory                                   outputFactory;
  private final BufferedReader                                  input;
  private final Map<MutationIdentifier, MutationStatusTestPair> previousResults   = new HashMap<MutationIdentifier, MutationStatusTestPair>();
  private final Map<ClassName, ClassHistory>                    previousClassPath = new HashMap<ClassName, ClassHistory>();

  public XStreamHistoryStore(final WriterFactory output,
      final Option<Reader> input) {
    this.outputFactory = output;
    this.input = createReader(input);
  }

  private static XStream configureXStream() {
    final XStream xstream = new XStream();
    xstream.alias("classHistory", ClassHistory.class);
    xstream.alias("fullClassId", HierarchicalClassId.class);
    xstream.alias("classId", ClassIdentifier.class);
    xstream.alias("name", ClassName.class);
    xstream.alias("result", IdResult.class);
    xstream.alias("statusTestPair", MutationStatusTestPair.class);
    xstream.alias("status", DetectionStatus.class);
    xstream.useAttributeFor(MutationStatusTestPair.class, "numberOfTestsRun");
    xstream.useAttributeFor(MutationStatusTestPair.class, "status");
    xstream.useAttributeFor(MutationStatusTestPair.class, "killingTest");
    xstream.useAttributeFor(ClassIdentifier.class, "name");
    xstream.useAttributeFor(ClassIdentifier.class, "hash");
    xstream.useAttributeFor(HierarchicalClassId.class, "hierarchicalHash");
    xstream.useAttributeFor(HierarchicalClassId.class, "classId");
    return xstream;
  }

  private BufferedReader createReader(final Option<Reader> input) {
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
      final ClassHistory coverage = new ClassHistory(each, coverageInfo
          .getCoverageIdForClass(each.getName()).toString(16));
      output.println(toXml(coverage));
    }
    output.flush();
  }

  @Override
  public void recordResult(final MutationResult result) {
    final PrintWriter output = this.outputFactory.create();
    output.println(toXml(new IdResult(result.getDetails().getId(), result
        .getStatusTestPair())));
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
        final IdResult result = (IdResult) fromXml(line);
        this.previousResults.put(result.id, result.status);
        line = this.input.readLine();
      }
    } catch (final IOException e) {
      LOG.warning("Could not read previous results");
    } catch (final StreamException e) {
      LOG.warning("Could not read previous results");
    }

  }

  private void restoreClassPath() {
    try {
      final long classPathSize = Long.valueOf(this.input.readLine());
      for (int i = 0; i != classPathSize; i++) {
        final ClassHistory coverage = (ClassHistory) fromXml(this.input
            .readLine());
        this.previousClassPath.put(coverage.getName(), coverage);
      }
    } catch (final IOException e) {
      LOG.warning("Could not read previous classpath");
    } catch (final StreamException e) {
      LOG.warning("Could not read previous classpath");
    }
  }

  private static Object fromXml(final String xml) {
    return XSTREAM_INSTANCE.fromXML(xml);
  }

  private static String toXml(final Object o) {
    final Writer writer = new StringWriter();
    XSTREAM_INSTANCE.marshal(o, new CompactWriter(writer));
    return writer.toString().replaceAll("\n", "");
  }

  private static class IdResult {
    final MutationIdentifier     id;
    final MutationStatusTestPair status;

    IdResult(final MutationIdentifier id, final MutationStatusTestPair status) {
      this.id = id;
      this.status = status;
    }

  }

}
