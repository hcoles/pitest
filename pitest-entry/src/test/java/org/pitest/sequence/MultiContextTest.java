package org.pitest.sequence;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class MultiContextTest {

    @Test
    public void obeysHashCodeEqualsContract() {
        EqualsVerifier.forClass(MultiContext.class)
                .withNonnullFields("slots")
                .withIgnoredFields("debug")
                .verify();
    }

}