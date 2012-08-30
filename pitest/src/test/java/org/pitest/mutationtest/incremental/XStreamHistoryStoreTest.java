package org.pitest.mutationtest.incremental;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.pitest.classinfo.ClassIdentifier;
import org.pitest.classinfo.ClassName;
import org.pitest.functional.Option;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.execute.MutationStatusTestPair;
import org.pitest.mutationtest.report.MutationTestResultMother;
import org.pitest.mutationtest.results.DetectionStatus;
import org.pitest.mutationtest.results.MutationResult;

public class XStreamHistoryStoreTest {

  private XStreamHistoryStore testee;

  private final Writer        output = new StringWriter();

  private final WriterFactory writerFactory = new WriterFactory() {

    public PrintWriter create() {
      return new PrintWriter(output);
    }

    public void close() {
      
    }
    
  };
  
  @Test
  public void shouldRecordAndRetrieveClassPath() {
    final ClassIdentifier foo = new ClassIdentifier(0,
        ClassName.fromString("foo"));
    final ClassIdentifier bar = new ClassIdentifier(0,
        ClassName.fromString("bar"));
    recordClassPathWithTestee(foo, bar);

    final Reader reader = new StringReader(this.output.toString());
    this.testee = new XStreamHistoryStore(writerFactory, Option.some(reader));
    this.testee.initialize();

    final Map<ClassName, ClassIdentifier> expected = new HashMap<ClassName, ClassIdentifier>();
    expected.put(foo.getName(), foo);
    expected.put(bar.getName(), bar);
    assertEquals(expected, this.testee.getHistoricClassPath());
  }

  @Test
  public void shouldRecordAndRetrieveResults() {
    final ClassIdentifier foo = new ClassIdentifier(0,
        ClassName.fromString("foo"));
    recordClassPathWithTestee(foo);

    final MutationResult mr = new MutationResult(
        MutationTestResultMother.createDetails("foo"),
        new MutationStatusTestPair(1, DetectionStatus.KILLED, "testName"));

    this.testee.recordResult(mr);

    final Reader reader = new StringReader(this.output.toString());
    this.testee = new XStreamHistoryStore(writerFactory, Option.some(reader));
    this.testee.initialize();
    final Map<MutationIdentifier, MutationStatusTestPair> expected = new HashMap<MutationIdentifier, MutationStatusTestPair>();
    expected.put(mr.getDetails().getId(), mr.getStatusTestPair());
    assertEquals(expected, this.testee.getHistoricResults());
  }

  @Test
  public void shouldNotAttemptToWriteToFileWhenNoneSupplied() {
    try {
      this.testee = new XStreamHistoryStore(writerFactory, Option.<Reader> none());
      this.testee.initialize();
    } catch (final Exception ex) {
      fail(ex.getMessage());
    }
  }
  
  private void recordClassPathWithTestee(
      final ClassIdentifier... classIdentifiers) {
    this.testee = new XStreamHistoryStore(writerFactory, Option.<Reader> none());
    final Collection<ClassIdentifier> ids = Arrays.asList(classIdentifiers);
    this.testee.recordClassPath(ids);
  }

}
