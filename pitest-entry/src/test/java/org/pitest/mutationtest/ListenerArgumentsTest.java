package org.pitest.mutationtest;

import org.junit.Test;
import org.pitest.mutationtest.verify.BuildIssue;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class ListenerArgumentsTest {

    @Test
    public void removesDuplicateBuildIssues() {
        List<BuildIssue> issues = asList(BuildIssue.issue("foo"), BuildIssue.issue("foo"));
        ListenerArguments underTest = new ListenerArguments(null, null, null, null, 0, false, null, issues);
        assertThat(underTest.issues()).containsExactly(BuildIssue.issue("foo"));
    }

    @Test
    public void ordersBuildIssuesByPriority() {
        BuildIssue a = new BuildIssue("foo", null, 5);
        BuildIssue b = new BuildIssue("important", null, 0);
        BuildIssue c = new BuildIssue("bar", null, 4);
        List<BuildIssue> issues = asList(a,b,c);
        ListenerArguments underTest = new ListenerArguments(null, null, null, null, 0, false, null, issues);
        assertThat(underTest.issues()).containsExactly(b, c, a);
    }
}