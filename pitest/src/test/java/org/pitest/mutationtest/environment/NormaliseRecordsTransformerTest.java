package org.pitest.mutationtest.environment;

import org.junit.Test;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.util.IsolationUtils;

import static org.assertj.core.api.Assertions.assertThat;


public class NormaliseRecordsTransformerTest {

    ClassLoader loader = IsolationUtils.getContextClassLoader();
    ClassByteArraySource bytes = new ClassloaderByteArraySource(this.loader);

    @Test
    public void doesNotTransformJDKClasses() {
        byte[] actual = transform(String.class);
        assertThat(actual).isNull();
    }

    private byte[] transform(final Class<?> clazz) {
        NormaliseRecordsTransformer testee = new NormaliseRecordsTransformer();
        return testee.transform(loader, clazz.getName(), null,
                null, this.bytes.getBytes(clazz.getName()).get());
    }
}