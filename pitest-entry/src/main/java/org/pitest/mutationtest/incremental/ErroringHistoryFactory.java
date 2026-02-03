package org.pitest.mutationtest.incremental;

import org.pitest.mutationtest.History;
import org.pitest.mutationtest.HistoryFactory;
import org.pitest.mutationtest.HistoryParams;
import org.pitest.plugin.Feature;
import org.pitest.util.PitError;

import java.io.Reader;
import java.util.Optional;

public class ErroringHistoryFactory implements HistoryFactory {
    @Override
    public History makeHistory(HistoryParams params, WriterFactory output, Optional<Reader> input) {
        throw new PitError("\nHistory has been enabled but no history plugin has been installed/activated.\n"
                + "If you are using https://www.arcmutate.com remember to activate the history plugin with +arcmutate_history");
    }

    @Override
    public Feature provides() {
        return Feature.named("HISTORY_NOT");
    }

    @Override
    public String description() {
        return "";
    }
}
