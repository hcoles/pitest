//package org.pitest.distributed;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNull;
//
//import java.net.InetAddress;
//import java.net.InetSocketAddress;
//import java.util.Collection;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.pitest.TestGroup;
//import org.pitest.TestResult;
//import org.pitest.distributed.slave.SlaveListener;
//import org.pitest.extension.Configuration;
//import org.pitest.extension.common.Configurations;
//import org.pitest.internal.ClassPath;
//import org.pitest.internal.TestClass;
//import org.pitest.testunit.TestUnit;
//import org.pitest.testunit.TestUnitState;
//
//import com.hazelcast.client.HazelcastClient;
//import com.hazelcast.core.HazelcastInstance;
//
//public class DistributedContainerIntegrationTest {
//
//  private SlaveListener        slave;
//  private DistributedContainer testee;
//
//  @Before
//  public void startSlave() {
//    this.slave = new SlaveListener();
//    this.slave.run();
//  }
//
//  @After
//  public void stopSlave() {
//    this.slave.stop();
//  }
//
//  private void createTestee() throws Exception {
//    final HazelcastInstance peer = HazelcastClient.newHazelcastClient("dev",
//        "dev-pass", InetAddress.getLocalHost().toString());
//
//    this.testee = new DistributedContainer(new ClassPath(),
//        new InetSocketAddress(10334), peer);
//
//  }
//
//  private TestGroup createTestGroupFromClass(final Class<?> clazz) {
//    final TestGroup group = new TestGroup();
//    for (final TestUnit tu : getTestUnitsIn(clazz)) {
//      group.add(tu);
//    }
//    return group;
//  }
//
//  private Collection<TestUnit> getTestUnitsIn(final Class<?> clazz) {
//    final Configuration c = Configurations.junitCompatible();
//    final TestClass tc = new TestClass(clazz, c);
//    return tc.getTestUnitsWithinClass();
//  }
//
//  public static class PassingTest {
//    @Test
//    public void pass() {
//
//    }
//  };
//
//  @Test
//  public void testStuff() throws Exception {
//    createTestee();
//
//    final TestGroup g = createTestGroupFromClass(PassingTest.class);
//
//    this.testee.submit(g);
//    this.testee.shutdown();
//
//    this.testee.feedbackQueue().take();
//    final TestResult actual = this.testee.feedbackQueue().take();
//    assertNull("should not get exception of type" + actual.getThrowable(),
//        actual.getThrowable());
//    assertEquals(TestUnitState.FINISHED, actual.getState());
//
//  }
// }
