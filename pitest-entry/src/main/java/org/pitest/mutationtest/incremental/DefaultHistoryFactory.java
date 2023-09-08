package org.pitest.mutationtest.incremental;

import org.pitest.classpath.CodeSource;
import org.pitest.mutationtest.History;
import org.pitest.mutationtest.HistoryFactory;

import java.io.Reader;
import java.util.Optional;

public class DefaultHistoryFactory implements HistoryFactory {
    @Override
    public History makeHistory(CodeSource code, WriterFactory output, Optional<Reader> input) {
        return new ObjectOutputStreamHistory(code, output, input);
    }

    @Override
    public String description() {
        return "Default history";
    }
}
