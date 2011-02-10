package org.pitest.coverage.execute;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.pitest.functional.SideEffect1;
import org.pitest.internal.IsolationUtils;
import org.pitest.util.Unchecked;

public class OutputToFile implements SideEffect1<CoverageResult> {

  private final static int           BUFFER_SIZE = 600;

  private final List<CoverageResult> buffer      = new ArrayList<CoverageResult>(
                                                     BUFFER_SIZE);

  private final Writer               w;

  public OutputToFile(final Writer w) {
    this.w = w;
  }

  public void apply(final CoverageResult a) {
    System.out.println(a.getTestUnitDescription() + " took "
        + a.getExecutionTime() + " ms and hit " + a.getCoverage().size()
        + " classes.");
    this.buffer.add(a);
    // this.bufferIndex++;

    if (this.buffer.size() >= BUFFER_SIZE) {
      writeToDisk();
    }

  }

  void writeToDisk() {
    try {
      this.w.append(IsolationUtils.toXml(this.buffer) + "\n");
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    }

    // int start = 0;
    // int end = 0;
    // while (end != this.buffer.size()) {
    // end = start + 600;
    // if (end > this.buffer.size()) {
    // end = this.buffer.size();
    // }
    // try {
    // this.w.append(IsolationUtils.toXml(this.buffer.subList(start, end))
    // + "\n");
    // start = end;
    // } catch (IOException e) {
    // throw Unchecked.translateCheckedException(e);
    // }
    //
    // }
    this.buffer.clear();

  }

}
