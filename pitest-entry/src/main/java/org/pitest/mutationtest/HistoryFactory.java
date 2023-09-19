package org.pitest.mutationtest;

import org.pitest.mutationtest.incremental.WriterFactory;
import org.pitest.plugin.ProvidesFeature;
import org.pitest.plugin.ToolClasspathPlugin;

import java.io.Reader;
import java.util.Optional;

public interface HistoryFactory extends ToolClasspathPlugin, ProvidesFeature {
    History makeHistory(HistoryParams params, WriterFactory output, Optional<Reader> input);
}
