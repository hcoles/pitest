package com.example.classloaders;

import org.junit.Test;
import org.pitest.classpath.ClassPath;
import org.pitest.mutationtest.execute.DefaultPITClassloader;

import java.util.function.IntSupplier;

import static org.junit.Assert.assertEquals;

public class MuteeInOtherClassloaderTest {

    @Test
    public void returns42() throws Exception {
        DefaultPITClassloader otherLoader = new DefaultPITClassloader(new ClassPath(), null);
        Class<?> clz = otherLoader.loadClass(MuteeInOtherClassloader.class.getName());
        IntSupplier underTest = (IntSupplier) clz.getConstructor().newInstance();
        assertEquals(42, underTest.getAsInt());
    }
}
