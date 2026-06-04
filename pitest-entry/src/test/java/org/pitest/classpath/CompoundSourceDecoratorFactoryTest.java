package org.pitest.classpath;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.plugin.Feature;
import org.pitest.plugin.FeatureSetting;
import org.pitest.plugin.ToggleStatus;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CompoundSourceDecoratorFactoryTest {

    @Mock
    CodeSourceDecoratorFactory factory1;

    @Mock
    CodeSourceDecoratorFactory factory2;

    @Mock
    CodeSourceDecorator decorator1;

    @Mock
    CodeSourceDecorator decorator2;

    @Mock
    CodeSource codeSource;

    @Mock
    CodeSource decorated1;

    @Mock
    CodeSource decorated2;

    @Mock
    ReportOptions data;

    CompoundSourceDecoratorFactory testee;

    @Before
    public void setUp() {
        when(factory1.provides()).thenReturn(Feature.named("f1").withOnByDefault(true));
        when(factory2.provides()).thenReturn(Feature.named("f2").withOnByDefault(true));

        when(factory1.createDecorator(any())).thenReturn(decorator1);
        when(factory2.createDecorator(any())).thenReturn(decorator2);

        when(decorator1.decorate(codeSource)).thenReturn(decorated1);
        when(decorator2.decorate(decorated1)).thenReturn(decorated2);
    }

    @Test
    public void appliesActiveDecoratorsInOrder() {
        testee = new CompoundSourceDecoratorFactory(Collections.emptyList(), Arrays.asList(factory1, factory2));

        CodeSourceDecorator composite = testee.createDecorator(data);
        CodeSource result = composite.decorate(codeSource);

        assertThat(result).isSameAs(decorated2);
        verify(decorator1).decorate(codeSource);
        verify(decorator2).decorate(decorated1);
    }

    @Test
    public void passesSettingsToFactories() {
        FeatureSetting setting1 = new FeatureSetting("f1", ToggleStatus.ACTIVATE, Collections.singletonMap("foo", Collections.singletonList("bar")));
        testee = new CompoundSourceDecoratorFactory(Arrays.asList(setting1), Arrays.asList(factory1));

        testee.createDecorator(data);

        ArgumentCaptor<CodeSourceParams> captor = ArgumentCaptor.forClass(CodeSourceParams.class);
        verify(factory1).createDecorator(captor.capture());

        CodeSourceParams params = captor.getValue();
        assertThat(params.conf()).isEqualTo(setting1);
        assertThat(params.data()).isSameAs(data);
    }

    @Test
    public void handlesNoActiveDecorators() {
        testee = new CompoundSourceDecoratorFactory(Collections.emptyList(), Collections.emptyList());

        CodeSourceDecorator composite = testee.createDecorator(data);
        CodeSource result = composite.decorate(codeSource);

        assertThat(result).isSameAs(codeSource);
    }
}