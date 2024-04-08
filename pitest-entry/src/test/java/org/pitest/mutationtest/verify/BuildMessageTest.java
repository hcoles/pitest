package org.pitest.mutationtest.verify;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;


public class BuildMessageTest {
    @Test
    public void obeysHashcodeEqualsContract() {
        EqualsVerifier.forClass(BuildMessage.class)
                .verify();
    }

    @Test
    public void sortsZeroPriorityFirst() {
        BuildMessage a = new BuildMessage("a","",10);
        BuildMessage b = new BuildMessage("b","",0);
        BuildMessage c = new BuildMessage("c","",5);

        List<BuildMessage> l = asList(a,b,c);
        Collections.sort(l);
        assertThat(l).containsExactly(b,c,a);
    }

    @Test
    public void includesURLInToStringWhenPresent() {
        BuildMessage underTest = new BuildMessage("text", "https://pitest.org", 0);
        assertThat(underTest.toString()).isEqualTo("text (https://pitest.org)");
    }

    @Test
    public void doesNotIncludeURLInToStringWhenNull() {
        BuildMessage underTest = new BuildMessage("text", null, 0);
        assertThat(underTest.toString()).isEqualTo("text");
    }
}