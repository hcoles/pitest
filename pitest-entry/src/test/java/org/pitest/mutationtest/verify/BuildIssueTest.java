package org.pitest.mutationtest.verify;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;


public class BuildIssueTest {
    @Test
    public void obeysHashcodeEqualsContract() {
        EqualsVerifier.forClass(BuildIssue.class)
                .verify();
    }

    @Test
    public void sortsZeroPriorityFirst() {
        BuildIssue a = new BuildIssue("a","",10);
        BuildIssue b = new BuildIssue("b","",0);
        BuildIssue c = new BuildIssue("c","",5);

        List<BuildIssue> l = asList(a,b,c);
        Collections.sort(l);
        assertThat(l).containsExactly(b,c,a);
    }
}