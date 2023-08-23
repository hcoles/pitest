package org.pitest.mutationtest.incremental;

import org.pitest.mutationtest.HistoryFactory;
import org.pitest.mutationtest.HistoryStore;

import java.io.Reader;
import java.util.Optional;

public class DefaultHistory implements HistoryFactory {
    @Override
    public HistoryStore makeHistory(WriterFactory output, Optional<Reader> input) {
        return new ObjectOutputStreamHistoryStore(output, input);
    }

    @Override
    public String description() {
        return "Default history";
    }
}
