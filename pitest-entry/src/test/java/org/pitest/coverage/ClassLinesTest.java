package org.pitest.coverage;


import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;
import org.pitest.classinfo.ClassName;

import java.util.Collections;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static java.util.Arrays.asList;

public class ClassLinesTest {

    @Test
    public void obeysEqualsHashcodeContract() {
        EqualsVerifier.forClass(ClassLines.class)
                .withNonnullFields("name")
                .withIgnoredFields("codeLines")
                .verify();
    }

    @Test
    public void relocateModifiesClassname() {
        // note relocate is used within external plugins
        ClassName bar = ClassName.fromString("bar");
        ClassLines underTest = new ClassLines(ClassName.fromString("foo"), Collections.emptySet());
        assertThat(underTest.relocate(bar)).isEqualTo(new ClassLines(bar, Collections.emptySet()));
    }

    @Test
    public void convertsToClassLines() {
        ClassName foo = ClassName.fromString("foo");
        ClassLines underTest = new ClassLines(foo, new HashSet<>(asList(1,2)));

        assertThat(underTest.asList()).containsExactly(new ClassLine(foo,1), new ClassLine(foo,2));
    }

    @Test
    public void reportsNumberOfCodeLines() {
        ClassName foo = ClassName.fromString("foo");
        ClassLines underTest = new ClassLines(foo, new HashSet<>(asList(1,2)));

        assertThat(underTest.getNumberOfCodeLines()).isEqualTo(2);
    }
}