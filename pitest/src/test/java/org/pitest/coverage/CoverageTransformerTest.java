package org.pitest.coverage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.instrument.IllegalClassFormatException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.classpath.OtherClassLoaderClassPathRoot;
import org.pitest.functional.predicate.False;
import org.pitest.util.IsolationUtils;
import org.pitest.util.StreamUtil;

import sun.pitest.CodeCoverageStore;
import sun.pitest.InvokeReceiver;

public class CoverageTransformerTest {

  private final ClassLoader          loader = IsolationUtils
                                                .getContextClassLoader();

  private final ClassByteArraySource bytes  = new ClassloaderByteArraySource(
                                                this.loader);

  @Mock
  private InvokeReceiver             invokeQueue;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    CodeCoverageStore.init(this.invokeQueue);
  }

  @After
  public void tearDown() {
    CodeCoverageStore.resetAllStaticState();
  }

  @Test
  public void shouldNotTransformClassesNotMatchingPredicate()
      throws IllegalClassFormatException {
    final CoverageTransformer testee = new CoverageTransformer(
        False.<String> instance());
    assertNull(testee.transform(null, "anything", null, null, null));
  }

  @Test
  public void shouldTransformClasseMatchingPredicate()
      throws IllegalClassFormatException {
    final CoverageTransformer testee = new CoverageTransformer(
        s -> true);
    final byte[] bs = this.bytes.getBytes(String.class.getName()).get();
    assertFalse(Arrays.equals(bs,
        testee.transform(null, "anything", null, null, bs)));
  }

  @Test
  public void shouldGenerateValidClasses() throws IllegalClassFormatException {
    assertValidClass(String.class);
    assertValidClass(Integer.class);
    assertValidClass(Vector.class);
    assertValidClass(ArrayList.class);
    assertValidClass(Collections.class);
    assertValidClass(ConcurrentHashMap.class);
    assertValidClass(Math.class);
  }

  private void assertValidClass(final Class<?> clazz)
      throws IllegalClassFormatException {
    final byte[] bs = transform(clazz);
    // printClass(bs);
    final StringWriter sw = new StringWriter();
    CheckClassAdapter.verify(new ClassReader(bs), false, new PrintWriter(sw));
    assertTrue(sw.toString(), sw.toString().length() == 0);

  }

  protected void printRaw(final Class<?> clazz) throws IOException {
    final OtherClassLoaderClassPathRoot r = new OtherClassLoaderClassPathRoot(
        IsolationUtils.getContextClassLoader());
    printClass(StreamUtil.streamToByteArray(r.getData(clazz.getName())));
  }

  protected void printClass(final byte[] bs) {
    final ClassReader reader = new ClassReader(bs);
    reader.accept(new TraceClassVisitor(null, new ASMifier(), new PrintWriter(
        System.out)), ClassReader.EXPAND_FRAMES);
  }

  private byte[] transform(final Class<?> clazz)
      throws IllegalClassFormatException {
    final CoverageTransformer testee = new CoverageTransformer(
        s -> true);
    final byte[] bs = testee.transform(this.loader, clazz.getName(), null,
        null, this.bytes.getBytes(clazz.getName()).get());
    return bs;
  }

}
