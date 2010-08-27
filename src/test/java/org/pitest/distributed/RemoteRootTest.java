package org.pitest.distributed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.distributed.master.MasterService;
import org.pitest.functional.Option;
import org.pitest.internal.IsolationUtils;

public class RemoteRootTest {

  private RemoteRoot    testee;

  @Mock
  private MasterService service;

  @Mock
  private ResourceCache cache;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.testee = new RemoteRoot(this.service, this.cache);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetClassNamesThrowsUnsupportedOperationException() {
    this.testee.classNames();
  }

  @Test
  public void testGetDataReturnsNullIfNullReturnedByRemoteClasspath()
      throws Exception {
    assertEquals(null, this.testee.getData("foo"));
  }

  @Test
  public void testGetDataReturnsInputStreamWhenDataAvailable() throws Exception {
    when(this.service.getClasspathData("foo")).thenReturn("bar".getBytes());
    assertNotNull(this.testee.getData("foo"));
  }

  @Test
  public void testGetResourceValueIfAvailableInCacheWithoutMakingRemoteCall()
      throws Exception {
    final URL expected = new URL("file:\\bar");
    when(this.cache.getResource("foo")).thenReturn(Option.someOrNone(expected));
    assertSame(expected, this.testee.getResource("foo"));
    verify(this.service, never()).getResourceData(anyString());
  }

  @Test
  public void testGetResourceReturnsNullIfNoValueInCacheOrAvailableRemotely()
      throws Exception {
    when(this.cache.getResource("foo")).thenReturn(Option.<URL> none());
    when(this.service.getResourceData("foo"))
        .thenReturn(Option.<byte[]> none());
    assertNull(this.testee.getResource("foo"));
  }

  @Test
  public void testGetResourceCachesRemoteDataWhenNoneAvailableInCache()
      throws Exception {
    final byte[] expected = "bar".getBytes();
    when(this.cache.getResource("foo")).thenReturn(Option.<URL> none());
    when(this.service.getResourceData("foo")).thenReturn(
        Option.someOrNone(expected));
    this.testee.getResource("foo");
    verify(this.cache).cacheResource("foo", expected);
  }

  @Test
  public void testCanSerializeToXmlAndBack() {
    IsolationUtils.cloneForLoader(this.testee, IsolationUtils
        .getContextClassLoader());
    // pass if we get here

  }
}
