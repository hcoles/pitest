package org.pitest.coverage.export;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.pitest.coverage.BlockCoverage;
import org.pitest.coverage.CoverageExporter;
import org.pitest.mutationtest.engine.Location;
import org.pitest.util.ResultOutputStrategy;
import org.pitest.util.StringUtil;
import org.pitest.util.Unchecked;

/**
 * Quick and dirty export of coverage data into XML
 */
public class DefaultCoverageExporter implements CoverageExporter {

  private final ResultOutputStrategy outputStrategy;

  public DefaultCoverageExporter(final ResultOutputStrategy outputStrategy) {
    this.outputStrategy = outputStrategy;
  }

  @Override
  public void recordCoverage(final Collection<BlockCoverage> coverage) {
    final Writer out = this.outputStrategy
        .createWriterForFile("linecoverage.xml");
    writeHeader(out);
    for (final BlockCoverage each : coverage) {
      writeLineCoverage(each, out);
    }

    writeFooterAndClose(out);
  }

  private void writeHeader(final Writer out) {
    write(out, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    write(out, "<coverage>\n");
  }

  private void writeLineCoverage(final BlockCoverage each, final Writer out) {
    Location l = each.getBlock().getLocation();
    write(
        out,
        "<block classname='" + l.getClassName().asJavaName() + "'"
            + " method='"
            + StringUtil.escapeBasicHtmlChars(l.getMethodName().name()) + StringUtil.escapeBasicHtmlChars(l.getMethodDesc())
            + "' number='" + each.getBlock().getBlock() + "'>");
    write(out, "<tests>\n");
    final List<String> ts = new ArrayList<String>(each.getTests());
    Collections.sort(ts);
    for (final String test : ts) {
      write(out, "<test name='" + test + "'/>\n");
    }
    write(out, "</tests>\n");
    write(out, "</block>\n");
  }

  private void writeFooterAndClose(final Writer out) {
    try {
      write(out, "</coverage>\n");
      out.close();
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

  private void write(final Writer out, final String value) {
    try {
      out.write(value);
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

}
