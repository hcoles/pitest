package org.pitest.sequence;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class StateContextTest {
    @Test
    public void obeysHashCodeEqualsContract() {
        EqualsVerifier.forClass(StateContext.class)
                .withNonnullFields("state", "context")
                .verify();
    }
}