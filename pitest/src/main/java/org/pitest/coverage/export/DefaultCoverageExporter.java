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
  
  private final ResultOutputStrategy outputStrategy;
  
  public DefaultCoverageExporter(final ResultOutputStrategy outputStrategy) {
    this.outputStrategy = outputStrategy;
  }

  
  public void recordCoverage(Collection<LineCoverage> coverage) {
    Writer out = outputStrategy.createWriterForFile("linecoverage.xml");
    writeHeader(out);
    for ( LineCoverage each : coverage ) {
      writeLineCoverage(each, out);
    }

    writeFooterAndClose(out);
  }

  private void writeHeader(Writer out) {
    write(out, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    write(out,"<coverage>\n");
  }

  private void writeLineCoverage(LineCoverage each, Writer out) {
    write(out, "<line classname='" + each.getClassLine().getClassName().asJavaName() + "'" + " number='" + each.getClassLine().getLineNumber() +"'>");
    write(out, "<tests>\n");
    List<String> ts = new ArrayList<String>(each.getTests());
    Collections.sort(ts);
    for ( String test : ts) {
      write(out, "<test name='" + test + "'/>\n");
    }
    write(out, "</tests>\n");
    write(out, "</line>\n");
  }
  
  private void writeFooterAndClose(Writer out) {
    try {
      write(out,"</coverage>\n");
      out.close();
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    }
  }
  
  private void write(Writer out, final String value) {
    try {
      out.write(value);
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

}
