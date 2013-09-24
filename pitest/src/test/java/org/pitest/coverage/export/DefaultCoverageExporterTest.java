package org.pitest.coverage.export;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.pitest.coverage.ClassLine;
import org.pitest.coverage.LineCoverage;
import org.pitest.util.ResultOutputStrategy;

public class DefaultCoverageExporterTest {

  private DefaultCoverageExporter testee;

  private final Writer            out = new StringWriter();

  @Before
  public void setup() {
    this.testee = new DefaultCoverageExporter(createOutputStrategy());
  }

  private ResultOutputStrategy createOutputStrategy() {
    return new ResultOutputStrategy() {

      public Writer createWriterForFile(final String sourceFile) {
        return DefaultCoverageExporterTest.this.out;
      }

    };
  }

  @Test
  public void shouldWriteValidXMLDocumentWhenNoCoverage() {
    this.testee.recordCoverage(Collections.<LineCoverage> emptyList());
    assertThat(this.out.toString(),
        containsString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
    assertThat(this.out.toString(), containsString("<coverage>"));
    assertThat(this.out.toString(), containsString("</coverage>"));
  }

  @Test
  public void shouldExportSuppliedCoverage() {
    final Collection<LineCoverage> coverage = Arrays.asList(
        new LineCoverage(new ClassLine("Foo", 1), Arrays.asList("Test1",
            "Test2")),
        new LineCoverage(new ClassLine("Bar", 2), Arrays.asList("Test3",
            "Test4")));
    this.testee.recordCoverage(coverage);

    assertThat(this.out.toString(),
        containsString("<line classname='Foo' number='1'>"));
    assertThat(this.out.toString(),
        containsString("<line classname='Bar' number='2'>"));
    assertThat(
        this.out.toString(),
        containsString("<tests>\n<test name='Test1'/>\n<test name='Test2'/>\n</tests>"));
    assertThat(
        this.out.toString(),
        containsString("<tests>\n<test name='Test3'/>\n<test name='Test4'/>\n</tests>"));
  }

}
