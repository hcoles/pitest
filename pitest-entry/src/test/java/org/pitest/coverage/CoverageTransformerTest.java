package org.pitest.coverage;

import org.junit.Test;
import org.mockito.Mockito;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.classpath.ClassloaderByteArraySource;
import sun.pitest.CodeCoverageStore;
import sun.pitest.InvokeReceiver;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

// additional tests for coverage transformer which are
// easier to write within pitest-entry due to access to
// ClassTree
public class CoverageTransformerTest {

    ClassloaderByteArraySource byteSource = ClassloaderByteArraySource.fromContext();
    CoverageTransformer underTest = new CoverageTransformer(s -> true);

    @Test
    public void doesNotDuplicateClinitWhenSynthetic() {

        byte[] bytes = bytesForClassWithSyntheticStaticInit();

        CodeCoverageStore.init(Mockito.mock(InvokeReceiver.class));

        byte[] transformed = underTest.transform(null, "anything", null, null, bytes);

        ClassTree instrumentedClass = ClassTree.fromBytes(transformed);

        List<MethodTree> clinitMethods = instrumentedClass.methods().stream()
                .filter(m -> m.rawNode().name.equals("<clinit>"))
                .collect(Collectors.toList());

        assertThat(clinitMethods).hasSize(1);
    }

    private byte[] bytesForClassWithSyntheticStaticInit() {
        ClassTree classWithStaticInit = ClassTree.fromBytes(byteSource.getBytes(HasStaticInit.class.getName()).get());
        MethodTree clinit = classWithStaticInit.methods().stream()
                .filter(m -> m.rawNode().name.equals("<clinit>"))
                .findAny()
                .get();

        clinit.rawNode().access = Opcodes.ACC_SYNTHETIC;

        byte[] bytes = asBytes(classWithStaticInit);
        return bytes;
    }

    private byte[] asBytes(ClassTree tree) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        tree.rawNode().accept(classWriter);
        return classWriter.toByteArray();
    }
}

class HasStaticInit {
    static String FOO = "";
}
