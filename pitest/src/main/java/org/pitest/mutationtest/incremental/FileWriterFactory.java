package org.pitest.mutationtest.incremental;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.pitest.util.Unchecked;

public class FileWriterFactory implements WriterFactory {

  private final File  file;
  private PrintWriter writer;

  public FileWriterFactory(final File file) {
    this.file = file;
  }

  @Override
  public PrintWriter create() {
    this.file.getParentFile().mkdirs();
    try {
      if (this.writer == null) {
        this.writer = new PrintWriter(new OutputStreamWriter(
            new FileOutputStream(this.file), "UTF-8"));
      }

      return this.writer;
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

  @Override
  public void close() {
    if (this.writer != null) {
      this.writer.close();
    }

  }

}
