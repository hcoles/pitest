package org.pitest.mutationtest.incremental;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.classinfo.ClassHash;
import org.pitest.classinfo.ClassIdentifier;
import org.pitest.classinfo.ClassName;
import org.pitest.classinfo.HierarchicalClassId;
import org.pitest.classpath.CodeSource;
import org.pitest.coverage.CoverageDatabase;
import java.util.Optional;
import java.util.stream.Collectors;

import org.pitest.mutationtest.ClassHistory;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.report.MutationTestResultMother;

public class ObjectOutputStreamHistoryTest {

    private static final String             COV           = BigInteger.TEN.toString(16);

    private ObjectOutputStreamHistory testee;

    @Mock
    private CoverageDatabase                coverage;

    @Mock
    private CodeSource code;

    private final Writer                    output        = new StringWriter();

    private final WriterFactory             writerFactory = new WriterFactory() {

        @Override
        public PrintWriter create() {
            return new PrintWriter(
                ObjectOutputStreamHistoryTest.this.output);
        }

        @Override
        public void close() {

        }

    };

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(this.coverage.getCoverageIdForClass(any(ClassName.class))).thenReturn(
            BigInteger.TEN);
    }

    @Test
    public void shouldRecordAndRetrieveClassPath() {
        final ClassHistory foo = new ClassHistory(new HierarchicalClassId(
            new ClassIdentifier(0, ClassName.fromString("foo")), ""), COV);
        final ClassHistory bar = new ClassHistory(new HierarchicalClassId(
            new ClassIdentifier(0, ClassName.fromString("bar")), ""), COV);

        recordClassPathWithTestee(foo.getId(), bar.getId());

        final Reader reader = new StringReader(this.output.toString());
        this.testee = new ObjectOutputStreamHistory(this.code, this.writerFactory,
            Optional.ofNullable(reader));
        this.testee.initialize();

        final Map<ClassName, ClassHistory> expected = new HashMap<>();
        expected.put(foo.getName(), foo);
        expected.put(bar.getName(), bar);
        assertEquals(expected, this.testee.getHistoricClassPath());
    }

    @Test
    public void shouldRecordAndRetrieveResults() {
        final HierarchicalClassId foo = new HierarchicalClassId(
            new ClassIdentifier(0, ClassName.fromString("foo")), "");
        recordClassPathWithTestee(foo);

        final MutationResult mr = new MutationResult(
            MutationTestResultMother.createDetails("foo"),
            new MutationStatusTestPair(1, DetectionStatus.KILLED, "testName"));

        this.testee.recordResult(mr);

        final Reader reader = new StringReader(this.output.toString());
        this.testee = new ObjectOutputStreamHistory(this.code, this.writerFactory,
            Optional.ofNullable(reader));
        this.testee.initialize();
        final Map<MutationIdentifier, MutationStatusTestPair> expected = new HashMap<>();
        expected.put(mr.getDetails().getId(), mr.getStatusTestPair());
        assertEquals(expected, this.testee.getHistoricResults());
    }

    @Test
    public void shouldNotAttemptToWriteToFileWhenNoneSupplied() {
        try {
            this.testee = new ObjectOutputStreamHistory(this.code, this.writerFactory,
                Optional.<Reader> empty());
            this.testee.initialize();
        } catch (final Exception ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void shouldReadCorruptFiles() throws IOException {
        final HierarchicalClassId foo = new HierarchicalClassId(
            new ClassIdentifier(0, ClassName.fromString("foo")), "");
        recordClassPathWithTestee(foo);

        final MutationResult mr = new MutationResult(
            MutationTestResultMother.createDetails("foo"),
            new MutationStatusTestPair(1, DetectionStatus.KILLED, "testName"));

        this.testee.recordResult(mr);
        this.output.append("rubbish");

        final Reader reader = new StringReader(this.output.toString());
        this.testee = new ObjectOutputStreamHistory(this.code, this.writerFactory,
            Optional.ofNullable(reader));
        this.testee.initialize();

        assertFalse(this.testee.getHistoricResults().isEmpty());
    }

    @Test
    public void doesNotErrorOnOldHistoryFile() throws IOException {
        final HierarchicalClassId foo = new HierarchicalClassId(
                new ClassIdentifier(0, ClassName.fromString("foo")), "");
        recordClassPathWithTestee(foo);

        final MutationResult mr = new MutationResult(
                MutationTestResultMother.createDetails("foo"),
                new MutationStatusTestPair(1, DetectionStatus.KILLED, "testName"));

        this.testee.recordResult(mr);
        this.output.append(pitest14HistoryFile());

        final Reader reader = new StringReader(this.output.toString());
        this.testee = new ObjectOutputStreamHistory(this.code, this.writerFactory,
                Optional.ofNullable(reader));
        this.testee.initialize();

        assertFalse(this.testee.getHistoricResults().isEmpty());
    }

    private String pitest14HistoryFile() {
        return "rO0ABXNyACRvcmcucGl0ZXN0Lm11dGF0aW9udGVzdC5DbGFzc0hpc3RvcnkAAAAAAAAAAQIAAkwACmNvdmVyYWdlSWR0ABJMamF2YS9sYW5nL1N0cmluZztM" +
                "AAJpZHQAKkxvcmcvcGl0ZXN0L2NsYXNzaW5mby9IaWVyYXJjaGljYWxDbGFzc0lkO3hwdAAIMTY1MmVhMDRzcgAob3JnLnBpdGVzdC5jbGFzc2luZm8uSGllc" +
                "mFyY2hpY2FsQ2xhc3NJZAAAAAAAAAABAgACTAAHY2xhc3NJZHQAJkxvcmcvcGl0ZXN0L2NsYXNzaW5mby9DbGFz" +
                "c0lkZW50aWZpZXI7TAAQaGllcmFyY2hpY2FsSGFzaHEAfgABeHBzcgAkb3JnLnBpdGVzdC5jbGFzc2luZm8uQ2xhc3NJZGVudGl" +
                "maWVyAAAAAAAAAAECAAJKAARoYXNoTAAEbmFtZXQAIExvcmcvcGl0ZXN0L2NsYXNzaW5mby9DbGFzc05hbWU7eHAAAAAAFlLqBHNyAB5" +
                "vcmcucGl0ZXN0LmNsYXNzaW5mby5DbGFzc05hbWUAAAAAAAAAAQIAAUwABG5hbWVxAH4AAXhwdAAkY29tL2V4YW1wbGUvbW9kdWxlYi9DYX" +
                "RDb252ZXJ0ZXJUZXN0dAAIMTY1MmVhMDQ=";
    }

    private void recordClassPathWithTestee(
        final HierarchicalClassId... classIdentifiers) {
        this.testee = new ObjectOutputStreamHistory(this.code, this.writerFactory,
            Optional.<Reader> empty());
        final Collection<ClassHash> ids = Arrays.asList(classIdentifiers).stream()
                .map(id -> new ClassHash() {
                    @Override
                    public ClassIdentifier getId() {
                        return id.getId();
                    }

                    @Override
                    public ClassName getName() {
                        return id.getName();
                    }

                    @Override
                    public BigInteger getDeepHash() {
                        return BigInteger.ZERO;
                    }

                    @Override
                    public HierarchicalClassId getHierarchicalId() {
                        return id;
                    }
                }).collect(Collectors.toList());

        when(code.fetchClassHashes(any(Collection.class))).thenReturn(ids);
        this.testee.processCoverage(this.coverage);
    }

}
