package org.pitest.mutationtest.incremental;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

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
      if (this.writer == null) { // 单例，同一个PrintWriter，不会覆盖文件
        this.writer = new PrintWriter(new OutputStreamWriter(
            new FileOutputStream(this.file), StandardCharsets.UTF_8));
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
