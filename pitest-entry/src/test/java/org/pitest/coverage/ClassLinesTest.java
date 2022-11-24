package org.pitest.coverage;


import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class ClassLinesTest {

    @Test
    public void obeysEqualsHashcodeContract() {
        EqualsVerifier.forClass(ClassLines.class)
                .withNonnullFields("name")
                .withIgnoredFields("codeLines")
                .verify();
    }
}