package org.pitest.coverage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
import org.objectweb.asm.util.CheckClassAdapter;
import org.pitest.boot.CodeCoverageStore;
import org.pitest.boot.InvokeReceiver;
import org.pitest.functional.predicate.False;
import org.pitest.functional.predicate.True;
import org.pitest.internal.ClassByteArraySource;
import org.pitest.internal.ClassloaderByteArraySource;
import org.pitest.internal.IsolationUtils;

public class CoverageTransformerTest {

  private final ClassByteArraySource bytes = new ClassloaderByteArraySource(
                                               IsolationUtils
                                                   .getContextClassLoader());

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
        True.<String> all());
    final byte[] bs = this.bytes.apply(String.class.getName()).value();
    assertFalse(Arrays.equals(bs,
        testee.transform(null, "anything", null, null, bs)));
  }

  public boolean foo(final int i) {
    boolean a = true;
    if (i < 4) {
      a = false;
    }
    return a;
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
    final CoverageTransformer testee = new CoverageTransformer(
        True.<String> all());
    final byte[] bs = testee.transform(null, clazz.getName(), null, null,
        this.bytes.apply(String.class.getName()).value());
    final StringWriter sw = new StringWriter();
    CheckClassAdapter.verify(new ClassReader(bs), false, new PrintWriter(sw));
    assertTrue(sw.toString(), sw.toString().length() == 0);
  }

}
