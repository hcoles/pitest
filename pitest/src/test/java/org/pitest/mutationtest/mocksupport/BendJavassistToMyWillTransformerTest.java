package org.pitest.mutationtest.mocksupport;

import java.lang.instrument.IllegalClassFormatException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.functional.predicate.Predicate;
import org.pitest.internal.ClassloaderByteArraySource;
import org.pitest.internal.IsolationUtils;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BendJavassistToMyWillTransformerTest {

  private BendJavassistToMyWillTransformer testee;
  
  @Mock
  private Predicate<String> filter;

  private byte[] bytes;


  
  @Before
  public void setUp () {
    MockitoAnnotations.initMocks(this);
    testee = new BendJavassistToMyWillTransformer(filter);
    ClassloaderByteArraySource source = new ClassloaderByteArraySource(IsolationUtils.getContextClassLoader());
    bytes = source.apply("java.lang.String").value();
  }
  
  @Test
  public void shouldNotTransformClassesNotMatchingFilter() throws IllegalClassFormatException {
    when(filter.apply(any(String.class))).thenReturn(false);
    assertNull(testee.transform(null, "foo", null, null, bytes));
  }
  
  @Test
  public void shouldTransformClassesMatchingFilter() throws IllegalClassFormatException {
    when(filter.apply(any(String.class))).thenReturn(true);
    assertFalse(null == testee.transform(null, "foo", null, null, bytes));
  }
}
