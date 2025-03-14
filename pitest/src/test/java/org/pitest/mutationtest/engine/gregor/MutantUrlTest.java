package org.pitest.mutationtest.engine.gregor;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;


public class MutantUrlTest {

    @Test
    public void obeysEqualsHashcodeContract() {
        EqualsVerifier.forClass(MutantUrl.class).verify();
    }
}