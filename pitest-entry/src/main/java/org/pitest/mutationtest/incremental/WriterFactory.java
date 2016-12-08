package org.pitest.mutationtest.incremental;

import java.io.PrintWriter;

public interface WriterFactory {

  PrintWriter create();

  void close();

}
