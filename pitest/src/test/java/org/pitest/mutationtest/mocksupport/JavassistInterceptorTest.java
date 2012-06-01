package org.pitest.mutationtest.mocksupport;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.MutationDetails;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.util.StreamUtil;


public class JavassistInterceptorTest {
  
  public class AClassWithAOpenClassFileMethod {
    public InputStream openClassfile(String name) {
      return new ByteArrayInputStream("original".getBytes());
    }
  }

  private AClassWithAOpenClassFileMethod interceptedClass;
  private Mutant mutant;
  
  @Before
  public void setUp() {
    interceptedClass = new AClassWithAOpenClassFileMethod();
    byte[] bytes = "replaced".getBytes();
    mutant = new Mutant(new MutationDetails(new MutationIdentifier("match", 0, "foo"), "foo", "foo", "foo", 0), bytes);
  }

  @Test
  public void shouldNotReplaceRequestedClassWithMutantWhenClassNameIsDifferent() throws IOException {
    JavassistInterceptor.setMutant(mutant);
    InputStream actual = JavassistInterceptor.openClassfile(interceptedClass, "nomatch");
    assertEquals(streamToString(actual), streamToString(interceptedClass.openClassfile("")));
  }
  
  @Test
  public void shouldNotReplaceRequestedClassWithMutantWhenNoMutantIsSet() throws IOException {
    JavassistInterceptor.setMutant(null);
    InputStream actual = JavassistInterceptor.openClassfile(interceptedClass, "nomatch");
    assertEquals(streamToString(actual), streamToString(interceptedClass.openClassfile("")));
  }
  
  @Test
  public void shouldReplaceRequestedClassWithMutantWhenClassNameMatches() throws IOException {
    JavassistInterceptor.setMutant(mutant);
    InputStream actual = JavassistInterceptor.openClassfile(interceptedClass, "match");
    
    assertEquals(streamToString(actual), "replaced");
  }
  
  private String streamToString(InputStream is) throws IOException {
    return new String(StreamUtil.streamToByteArray(is));
  }
  
}
