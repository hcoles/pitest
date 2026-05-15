package org.pitest.mutationtest.build;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pitest.classpath.CodeSource;
import org.pitest.classpath.DefaultCodeSource;
import org.pitest.classpath.ProjectClassPaths;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.plugin.Feature;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pitest.mutationtest.engine.MutationDetailsMother.aMutationDetail;

@RunWith(MockitoJUnitRunner.class)
public class CompoundProjectMutationFilterTest {

  @Mock
  ProjectMutationFilter first;

  @Mock
  ProjectMutationFilter second;

  @Test
  public void initialisesAllChildren() {
    CompoundProjectMutationFilter testee = new CompoundProjectMutationFilter(Arrays.asList(first, second));
    CodeSource source = new DefaultCodeSource(new ProjectClassPaths(null, null, null));

    testee.initialise(source);

    verify(first).initialise(source);
    verify(second).initialise(source);
  }

  @Test
  public void chainsFiltersThroughAllChildrenInOrder() {
    CompoundProjectMutationFilter testee = new CompoundProjectMutationFilter(Arrays.asList(first, second));

    Collection<MutationDetails> initial = aMutationDetail().build(3);
    Collection<MutationDetails> afterFirst = aMutationDetail().build(2);
    Collection<MutationDetails> afterSecond = aMutationDetail().build(1);

    when(first.intercept(initial)).thenReturn(afterFirst);
    when(second.intercept(afterFirst)).thenReturn(afterSecond);

    assertThat(testee.intercept(initial)).isEqualTo(afterSecond);
  }

  @Test
  public void returnsInputUnchangedWhenNoChildren() {
    CompoundProjectMutationFilter testee = new CompoundProjectMutationFilter(Collections.emptyList());
    Collection<MutationDetails> mutations = aMutationDetail().build(3);

    assertThat(testee.intercept(mutations)).isEqualTo(mutations);
  }

  @Test
  public void passthroughReturnsSameInstance() {
    Collection<MutationDetails> mutations = aMutationDetail().build(3);

    assertThat(CompoundProjectMutationFilter.passThrough().intercept(mutations)).isSameAs(mutations);
  }

  @Test
  public void filtersChildren() {
    when(first.type()).thenReturn(InterceptorType.FILTER);
    when(second.type()).thenReturn(InterceptorType.OTHER);

    CompoundProjectMutationFilter testee = new CompoundProjectMutationFilter(List.of(first, second))
            .filter(i -> i == InterceptorType.FILTER);

    testee.intercept(aMutationDetail().build(3));

    verify(first).intercept(any());
    verify(second, never()).intercept(any());
  }



}
