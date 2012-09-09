package org.pitest.coverage.export;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.pitest.mutationtest.report.ResultOutputStrategy;
import org.pitest.util.Unchecked;

/**
 * Quick and dirty export of coverage data into XML
 */
public class DefaultCoverageExporter implements CoverageExporter {
  
  private final Writer out;
  
  public DefaultCoverageExporter(final ResultOutputStrategy outputStrategy) {
    this(outputStrategy.createWriterForFile("linecoverage.xml"));
  }

  public DefaultCoverageExporter(final Writer out) {
    this.out = out;
  }
  
  public void recordCoverage(Collection<LineCoverage> coverage) {
    writeHeader();
    for ( LineCoverage each : coverage ) {
      writeLineCoverage(each);
    }

    writeFooterAndClose();
  }

  private void writeHeader() {
    write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    write("<coverage>\n");
  }

  private void writeLineCoverage(LineCoverage each) {
    write("<line classname='" + each.getClassLine().getClassName().asJavaName() + "'" + " number='" + each.getClassLine().getLineNumber() +"'>");
    write("<tests>\n");
    List<String> ts = new ArrayList<String>(each.getTests());
    Collections.sort(ts);
    for ( String test : ts) {
      write("<test name='" + test + "'/>\n");
    }
    write("</tests>\n");
    write("</line>\n");
  }
  
  private void writeFooterAndClose() {
    try {
      write("</coverage>\n");
      this.out.close();
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    }
  }
  
  private void write(final String value) {
    try {
      this.out.write(value);
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

}
