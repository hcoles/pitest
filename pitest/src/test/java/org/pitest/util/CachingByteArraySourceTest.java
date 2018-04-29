package org.pitest.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pitest.classinfo.CachingByteArraySource;
import org.pitest.classinfo.ClassByteArraySource;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class CachingByteArraySourceTest {

  @Mock
  ClassByteArraySource source;

  @Test
  public void shouldReturnBytesFromChild() {
    final Optional<byte[]> childResult = Optional.ofNullable(new byte[0]);

    when(this.source.getBytes("someClass")).thenReturn(childResult);

    final CachingByteArraySource testee = new CachingByteArraySource(this.source, 2);

    assertThat(testee.getBytes("someClass")).isSameAs(childResult);
  }

  @Test
  public void shouldCacheByteFromChild() {
    when(this.source.getBytes("someClass")).thenReturn(Optional.ofNullable(new byte[0]));

    final CachingByteArraySource testee = new CachingByteArraySource(this.source, 2);

    testee.getBytes("someClass");
    testee.getBytes("someClass");
    testee.getBytes("someClass");

    verify(this.source, times(1)).getBytes("someClass");
  }

}
