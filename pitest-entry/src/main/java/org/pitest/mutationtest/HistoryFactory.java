package org.pitest.mutationtest;

import org.pitest.classpath.CodeSource;
import org.pitest.mutationtest.incremental.WriterFactory;
import org.pitest.plugin.ToolClasspathPlugin;

import java.io.Reader;
import java.util.Optional;

public interface HistoryFactory extends ToolClasspathPlugin {
    History makeHistory(CodeSource code, WriterFactory output, Optional<Reader> input);
}
