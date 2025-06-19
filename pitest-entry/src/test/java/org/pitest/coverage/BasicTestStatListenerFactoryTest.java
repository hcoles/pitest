package org.pitest.coverage;

import org.junit.Test;
import org.pitest.mutationtest.config.PluginServices;

import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;

public class BasicTestStatListenerFactoryTest {

    @Test
    public void isOnChain() {
        List<TestStatListenerFactory> allInterceptors = PluginServices.makeForContextLoader()
                .findTestStatListeners();

        assertThat(allInterceptors).anyMatch(c -> c instanceof BasicTestStatListenerFactory);
    }

    @Test
    public void isOnByDefault() {
        BasicTestStatListenerFactory testee = new BasicTestStatListenerFactory();
        assertThat(testee.provides().isOnByDefault()).isTrue();
    }

}