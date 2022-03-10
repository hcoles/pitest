package com.example.emptyreturns;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

public class AlreadyReturnsEmptyOptionalInTryWithResourcesBlock {
    public Optional<String> a() throws IOException {
        try(ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            Double.parseDouble("12");
            if (os.size() > 42) {
                return Optional.empty();
            }
            return Optional.of("foo");
        }
    }

}
