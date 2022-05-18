package org.pitest.sequence;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class Context1Test {

    @Test
    public void obeysHashCodeEqualsContract() {
        EqualsVerifier.forClass(Context1.class)
                .withNonnullFields("slot")
                .withIgnoredFields("debug")
                .verify();
    }
}