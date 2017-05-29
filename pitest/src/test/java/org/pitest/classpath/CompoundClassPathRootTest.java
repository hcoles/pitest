package org.pitest.classpath;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class CompoundClassPathRootTest {

  private CompoundClassPathRoot testee;

  @Mock
  private ClassPathRoot         child1;

  @Mock
  private ClassPathRoot         child2;
  
  @Mock
  private IOHeavyRoot           heavyChild;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.testee = new CompoundClassPathRoot(Arrays.asList(this.child1,heavyChild,
        this.child2));
  }

  @Test
  public void shouldReturnNamesOfAllClassesKnownByChildren() {

    when(this.child1.classNames()).thenReturn(Collections.singletonList("Foo"));
    when(this.child2.classNames()).thenReturn(Collections.singletonList("Bar"));
    when(this.heavyChild.classNames()).thenReturn(Collections.singletonList("Heavy"));

    assertThat(this.testee.classNames()).containsOnly("Foo", "Bar", "Heavy");

  }

  @Test
  public void shouldReturnNullWhenNoChildCanSupplyData() throws IOException {
    assertThat(this.testee.getData("unknown")).isNull();
  }

  @Test
  public void shouldReturnNullWhenNoChildCanSupplyResource() throws IOException {
    assertThat(this.testee.getResource("unknown")).isNull();
  }

  @Test
  public void shouldReturnClassDataFromChildren() throws IOException {
    when(this.child1.getData(any(String.class))).thenReturn(null);
    final InputStream is = Mockito.mock(InputStream.class);
    when(this.child1.getData(any(String.class))).thenReturn(is);
    assertThat(this.testee.getData("Foo")).isSameAs(is);
  }

  @Test
  public void shouldReturnResourcesFromChildren() throws IOException {
    when(this.child1.getResource(any(String.class))).thenReturn(null);
    final URL url = new URL("http://localhost");
    when(this.child1.getResource(any(String.class))).thenReturn(url);
    assertThat(this.testee.getResource("Foo")).isSameAs(url);
  }
  
  @Test
  public void shouldNotQueryHeavyRootsForClassesTheyDoNotContain() throws IOException {
    when(this.child1.classNames()).thenReturn(Collections.singletonList("Foo"));
    when(this.heavyChild.classNames()).thenReturn(Collections.singletonList("Heavy"));
    when(this.child2.classNames()).thenReturn(Collections.singletonList("Bar"));


    testee.getData("Bar");
    verify(heavyChild,never()).getData("Bar");
    
    testee.getData("Heavy");    
    verify(heavyChild).getData("Heavy");
  }

}

 
