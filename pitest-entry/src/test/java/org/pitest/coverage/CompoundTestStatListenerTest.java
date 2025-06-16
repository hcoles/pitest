package org.pitest.coverage;

import org.junit.Test;
import org.mockito.Mockito;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class CompoundTestStatListenerTest {

    TestStatListener a = Mockito.mock(TestStatListener.class);
    TestStatListener b = Mockito.mock(TestStatListener.class);
    CompoundTestStatListener testee = new CompoundTestStatListener(asList(a,b));

    @Test
    public void delegatesAcceptToChildren() {
        CoverageResult cr = CoverageMother.aCoverageResult().build();
        testee.accept(cr);

        Mockito.verify(a).accept(cr);
        Mockito.verify(b).accept(cr);
    }

    @Test
    public void collectsMessagesFromChildren() {
        when(a.messages()).thenReturn(asList("a"));
        when(b.messages()).thenReturn(asList("b"));

        assertThat(testee.messages()).containsExactly("a","b");
    }

    @Test
    public void delegatesEndToChildren() {
        testee.end();

        Mockito.verify(a).end();
        Mockito.verify(b).end();
    }
}