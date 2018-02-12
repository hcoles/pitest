package org.pitest.mutationtest.mocksupport;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.lang.instrument.IllegalClassFormatException;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.util.IsolationUtils;

public class BendJavassistToMyWillTransformerTest {

  private BendJavassistToMyWillTransformer testee;

  @Mock
  private Predicate<String>                filter;

  private byte[]                           bytes;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.testee = new BendJavassistToMyWillTransformer(this.filter, JavassistInputStreamInterceptorAdapater.inputStreamAdapterSupplier(JavassistInterceptor.class));
    final ClassloaderByteArraySource source = new ClassloaderByteArraySource(
        IsolationUtils.getContextClassLoader());
    this.bytes = source.getBytes("java.lang.String").get();
  }

  @Test
  public void shouldNotTransformClassesNotMatchingFilter()
      throws IllegalClassFormatException {
    when(this.filter.test(any(String.class))).thenReturn(false);
    assertNull(this.testee.transform(null, "foo", null, null, this.bytes));
  }

  @Test
  public void shouldTransformClassesMatchingFilter()
      throws IllegalClassFormatException {
    when(this.filter.test(any(String.class))).thenReturn(true);
    assertFalse(null == this.testee.transform(null, "foo", null, null,
        this.bytes));
  }


}
