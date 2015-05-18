package org.pitest.mutationtest.incremental;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.StreamException;
import com.thoughtworks.xstream.io.xml.CompactWriter;
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
import org.pitest.util.PitXmlDriver;
import org.pitest.util.Unchecked;

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

public class XStreamHistoryStore implements HistoryStore {

  private static final Logger  LOG              = Log.getLogger();
  private static final XStream XSTREAM_INSTANCE = configureXStream();

  private final WriterFactory  outputFactory;
  private final BufferedReader input;
  private final Map<MutationIdentifier, MutationStatusTestPair> previousResults   = new HashMap<MutationIdentifier, MutationStatusTestPair>();
  private final Map<ClassName, ClassHistory>                    previousClassPath = new HashMap<ClassName, ClassHistory>();

  public XStreamHistoryStore(WriterFactory output, Option<Reader> input) {
    this.outputFactory = output;
    this.input = createReader(input);
  }

  private static XStream configureXStream() {
    XStream xstream = new XStream(new PitXmlDriver());

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

  public void recordClassPath(Collection<HierarchicalClassId> ids, CoverageDatabase coverageInfo) {
    PrintWriter output = outputFactory.create();
    output.println(ids.size());
    for (HierarchicalClassId each : ids) {
      String coverageId = coverageInfo.getCoverageIdForClass(each.getName()).toString(16);
      ClassHistory coverage = new ClassHistory(each, coverageId);
      output.println(toXml(coverage));
    }
    output.flush();
  }

  public void recordResult(MutationResult result) {
    PrintWriter output = outputFactory.create();
    MutationIdentifier mutationIdentifier = result.getDetails().getId();
    MutationStatusTestPair statusTestPair = result.getStatusTestPair();
    output.println(toXml(new IdResult(mutationIdentifier,statusTestPair)));
    output.flush();
  }

  public Map<MutationIdentifier, MutationStatusTestPair> getHistoricResults() {
    return previousResults;
  }

  public Map<ClassName, ClassHistory> getHistoricClassPath() {
    return previousClassPath;
  }

  public void initialize() {
    if (input != null) {
      restoreClassPath();
      restoreResults();
      try {
        input.close();
      } catch (IOException e) {
        throw Unchecked.translateCheckedException(e);
      }
    }
  }

  private void restoreResults() {
    String line;
    try {
      line = input.readLine();
      while (line != null) {
        IdResult result = (IdResult) fromXml(line);
        previousResults.put(result.id, result.status);
        line = input.readLine();
      }
    } catch (IOException e) {
      LOG.warning("Could not read previous results");
    } catch (StreamException e) {
      LOG.warning("Could not read previous results");
    }
  }

  private void restoreClassPath() {
    try {
      long classPathSize = Long.valueOf(input.readLine());
      for (int i = 0; i != classPathSize; i++) {
        ClassHistory coverage = (ClassHistory) fromXml(input.readLine());
        previousClassPath.put(coverage.getName(), coverage);
      }
    } catch (IOException e) {
      LOG.warning("Could not read previous classpath");
    } catch (StreamException e) {
      LOG.warning("Could not read previous classpath");
    }
  }

  private static Object fromXml(String xml) {
    return XSTREAM_INSTANCE.fromXML(xml);
  }

  private static String toXml(Object o) {
    Writer writer = new StringWriter();
    XSTREAM_INSTANCE.marshal(o, new CompactWriter(writer));
    return writer.toString().replaceAll("\n", "");
  }

  private static class IdResult {
    final MutationIdentifier     id;
    final MutationStatusTestPair status;

    IdResult(MutationIdentifier id, MutationStatusTestPair status) {
      this.id = id;
      this.status = status;
    }
  }
}
