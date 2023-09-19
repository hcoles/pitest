package org.pitest.mutationtest.incremental;

import org.pitest.mutationtest.History;
import org.pitest.mutationtest.HistoryFactory;
import org.pitest.mutationtest.HistoryParams;
import org.pitest.plugin.Feature;

import java.io.Reader;
import java.util.Optional;

public class DefaultHistoryFactory implements HistoryFactory {
    @Override
    public History makeHistory(HistoryParams params, WriterFactory output, Optional<Reader> input) {
        return new ObjectOutputStreamHistory(params.code(), output, input);
    }

    @Override
    public String description() {
        return "Default history";
    }

    @Override
    public Feature provides() {
        return Feature.named("default_history")
                .withOnByDefault(true)
                .asInternalFeature()
                .withDescription(description());
    }
}
