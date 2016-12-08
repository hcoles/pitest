package org.pitest.classinfo;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;

public class NameToClassInfoTest {

  @Test
  public void shouldFetchClassWhenApplied() {
    final Repository repository = mock(Repository.class);
    final NameToClassInfo testee = new NameToClassInfo(repository);
    testee.apply(ClassName.fromString("foo"));
    verify(repository).fetchClass(ClassName.fromString("foo"));
  }

}
