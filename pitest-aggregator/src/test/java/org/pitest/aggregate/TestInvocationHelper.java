package org.pitest.aggregate;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import org.pitest.util.ResultOutputStrategy;
import org.pitest.util.Unchecked;

public class TestInvocationHelper {

  public static ResultOutputStrategy getResultOutputStrategy() {
    return sourceFile -> new Writer() {

      @Override
      public void write(final char[] cbuf, final int off, final int len) throws IOException {
        // ignore
      }

      @Override
      public void flush() throws IOException {
        // ignore
      }

      @Override
      public void close() throws IOException {
        // ignore
      }
    };
  }

  public static File getMutationFile() {
    try {
      return new File(ReportAggregatorBuilderTest.class.getResource("/full-data/mutations.xml").toURI());
    } catch (final Exception e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

  public static File getCoverageFile() {
    try {
      return new File(ReportAggregatorBuilderTest.class.getResource("/full-data/linecoverage.xml").toURI());
    } catch (final Exception e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

  public static File getSourceDirectory() {
    return new File("src" + File.separator + "main" + File.separator + "java");
  }

  public static File getTestSourceDirectory() {
    return new File("src" + File.separator + "test" + File.separator + "java");
  }

  public static File getCompiledDirectory() {
    try {
      return new File(ReportAggregatorBuilderTest.class.getResource("/org/pitest/aggregate/DataLoader.class").toURI()).getParentFile() // aggregate
          .getParentFile() // pitest
          .getParentFile() // org
          .getParentFile(); // classes
    } catch (final Exception e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

  public static File getTestCompiledDirectory() {
    try {
      return new File(ReportAggregatorBuilderTest.class.getResource("/").toURI());
    } catch (final Exception e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

}
