package org.pitest.sequence;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class ContextTest {

    @Test
    public void obeysHashCodeEqualsContract() {
        EqualsVerifier.forClass(Context.class)
                .withNonnullFields("slots")
                .withIgnoredFields("debug")
                .verify();
    }

}