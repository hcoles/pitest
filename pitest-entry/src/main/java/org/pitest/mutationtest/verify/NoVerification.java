package org.pitest.mutationtest.verify;

import java.util.Collections;
import java.util.List;

public class NoVerification implements BuildVerifier {
    @Override
    public List<String> verify() {
        return Collections.emptyList();
    }
}
