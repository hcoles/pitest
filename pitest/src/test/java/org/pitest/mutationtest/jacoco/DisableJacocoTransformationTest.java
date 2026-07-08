package org.pitest.mutationtest.jacoco;

import org.junit.Test;
import org.pitest.mutationtest.config.ClientPluginServices;

import static org.assertj.core.api.Assertions.assertThat;

public class DisableJacocoTransformationTest {

    @Test
    public void transformationIsOnChain() {
        ClientPluginServices services = ClientPluginServices.makeForContextLoader();
        assertThat(services.findTransformations()).hasAtLeastOneElementOfType(DisableJacocoTransformation.class);
    }

    @Test
    public void hasSensibleDescription() {
        assertThat(new DisableJacocoTransformation().description()).isEqualTo("Disable JaCoCo");
    }
}