package org.pitest.mutationtest.mocksupport;

import static org.junit.Assert.assertEquals;
import static org.pitest.mutationtest.LocationMother.aLocation;
import static org.pitest.mutationtest.LocationMother.aMutationId;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.util.StreamUtil;

public class JavassistInterceptorTest {

  public class AClassWithAOpenClassFileMethod {
    public InputStream openClassfile(final String name) {
      return new ByteArrayInputStream("original".getBytes());
    }
  }

  private AClassWithAOpenClassFileMethod interceptedClass;
  private Mutant                         mutant;

  @Before
  public void setUp() {
    this.interceptedClass = new AClassWithAOpenClassFileMethod();
    final byte[] bytes = "replaced".getBytes();
    this.mutant = new Mutant(new MutationDetails(aMutationId().withLocation(
        aLocation().withClass(ClassName.fromString("match"))).build(), "foo",
        "foo", 0, 0), bytes);
  }

  @Test
  public void shouldNotReplaceRequestedClassWithMutantWhenClassNameIsDifferent()
      throws IOException {
    JavassistInterceptor.setMutant(this.mutant);
    final InputStream actual = JavassistInterceptor.openClassfile(
        this.interceptedClass, "nomatch");
    assertEquals(streamToString(actual),
        streamToString(this.interceptedClass.openClassfile("")));
  }

  @Test
  public void shouldNotReplaceRequestedClassWithMutantWhenNoMutantIsSet()
      throws IOException {
    JavassistInterceptor.setMutant(null);
    final InputStream actual = JavassistInterceptor.openClassfile(
        this.interceptedClass, "nomatch");
    assertEquals(streamToString(actual),
        streamToString(this.interceptedClass.openClassfile("")));
  }

  @Test
  public void shouldReplaceRequestedClassWithMutantWhenClassNameMatches()
      throws IOException {
    JavassistInterceptor.setMutant(this.mutant);
    final InputStream actual = JavassistInterceptor.openClassfile(
        this.interceptedClass, "match");

    assertEquals(streamToString(actual), "replaced");
  }

  private String streamToString(final InputStream is) throws IOException {
    return new String(StreamUtil.streamToByteArray(is));
  }

}
