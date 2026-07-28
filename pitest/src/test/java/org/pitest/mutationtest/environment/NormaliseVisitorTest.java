package org.pitest.mutationtest.environment;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.pitest.bytecode.FrameOptions;
import org.pitest.classinfo.ComputeClassWriter;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.util.IsolationUtils;
import org.pitest.util.ResourceFolderByteArraySource;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class NormaliseVisitorTest {

    @Test
    public void recognisesRecords() {
        byte[] aRecord = new ResourceFolderByteArraySource().getBytes("SafeRange").get();
        var actual = transform(aRecord);
        assertThat(actual.isRecord).isTrue();
    }

    @Test
    public void recognisesNonRecord() {
        byte[] bytes = ClassloaderByteArraySource.fromContext()
                .getBytes("com/example/JUnitThreeTest").get();
        var actual = transform(bytes);
        assertThat(actual.isRecord).isFalse();
    }

    private NormaliseVisitor transform(byte[] input) {
        final ClassReader reader = new ClassReader(input);
        final ClassWriter writer = new ComputeClassWriter(
                new ClassloaderByteArraySource(IsolationUtils.getContextClassLoader()), new HashMap<>(),
                FrameOptions.pickFlags(input));

        var normaliser = new NormaliseVisitor(writer);
        reader.accept(normaliser, ClassReader.EXPAND_FRAMES);
        return normaliser;
    }

}