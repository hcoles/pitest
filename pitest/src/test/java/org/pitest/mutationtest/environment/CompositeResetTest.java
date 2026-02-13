package org.pitest.mutationtest.environment;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.pitest.mutationtest.engine.Mutant;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CompositeResetTest {

    private CompositeReset testee;

    private ResetEnvironment child1 = Mockito.mock(ResetEnvironment.class);

    private ResetEnvironment child2 = Mockito.mock(ResetEnvironment.class);

    private Mutant mutant;

    @Test
    public void callsAllChildrenForReset() {
        testee = new CompositeReset(Arrays.asList(child1, child2));
        testee.resetFor(mutant, aLoader());
        verify(child1).resetFor(mutant, aLoader());
        verify(child2).resetFor(mutant, aLoader());
    }

    @Test
    public void ordersChildrenByPriorityForReset() {
        when(child1.priority()).thenReturn(10);
        when(child2.priority()).thenReturn(5);

        testee = new CompositeReset(Arrays.asList(child1, child2));
        testee.resetFor(mutant, aLoader());

        InOrder inOrder = Mockito.inOrder(child1, child2);
        inOrder.verify(child2).resetFor(mutant, aLoader());
        inOrder.verify(child1).resetFor(mutant, aLoader());
    }

    @Test
    public void ordersChildrenByPriorityInverseForReset() {
        when(child1.priority()).thenReturn(5);
        when(child2.priority()).thenReturn(10);

        testee = new CompositeReset(Arrays.asList(child1, child2));
        testee.resetFor(mutant, aLoader());

        InOrder inOrder = Mockito.inOrder(child1, child2);
        inOrder.verify(child1).resetFor(mutant, aLoader());
        inOrder.verify(child2).resetFor(mutant, aLoader());
    }

    @Test
    public void handlesEmptyListOfChildrenForReset() {
        testee = new CompositeReset(Collections.emptyList());
        assertThatCode(() ->testee.resetFor(mutant, aLoader()))
                .doesNotThrowAnyException();
    }

    @Test
    public void callsAllChildrenForFinish() {
        testee = new CompositeReset(Arrays.asList(child1, child2));
        testee.finishFor(mutant, aLoader());
        verify(child1).finishFor(mutant, aLoader());
        verify(child2).finishFor(mutant, aLoader());
    }

    @Test
    public void ordersChildrenByPriorityForFinish() {
        when(child1.priority()).thenReturn(10);
        when(child2.priority()).thenReturn(5);

        testee = new CompositeReset(Arrays.asList(child1, child2));
        testee.finishFor(mutant, aLoader());

        InOrder inOrder = Mockito.inOrder(child1, child2);
        inOrder.verify(child2).finishFor(mutant, aLoader());
        inOrder.verify(child1).finishFor(mutant, aLoader());
    }

    @Test
    public void ordersChildrenByPriorityInverseForFinish() {
        when(child1.priority()).thenReturn(5);
        when(child2.priority()).thenReturn(10);

        testee = new CompositeReset(Arrays.asList(child1, child2));
        testee.finishFor(mutant, aLoader());

        InOrder inOrder = Mockito.inOrder(child1, child2);
        inOrder.verify(child1).finishFor(mutant, aLoader());
        inOrder.verify(child2).finishFor(mutant, aLoader());
    }

    @Test
    public void handlesEmptyListOfChildrenForFinish() {
        testee = new CompositeReset(Collections.emptyList());
        assertThatCode(() ->testee.finishFor(mutant, aLoader()))
                .doesNotThrowAnyException();
    }

    private ClassLoader aLoader() {
        // good enough
        return null;
    }

}