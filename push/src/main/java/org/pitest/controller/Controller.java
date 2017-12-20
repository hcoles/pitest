package org.pitest.controller;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.rmi.registry.LocateRegistry;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MethodName;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.util.Unchecked;

public class Controller {
  public static void main(String[] args) {

    try {
      // slight chance another process will steal the port before we use it
      // but we only do this once
      int myPort = findPort();
      System.out.println("Controller on " + myPort);
      JMXConnectorServer server = createJmxConnectorServer(myPort);
      server.start();
      
      MinionPool pool = new MinionPool(2, new MinionFactory(myPort));
      registerMXBean(pool);
      
      Location l = Location.location(ClassName.fromString(""), MethodName.fromString(""), "");
      MutationIdentifier id = new MutationIdentifier(l, 1, "");
      MutationIdentifier death = new MutationIdentifier(l, 1, "death");
      
      pool.submit(Arrays.asList(id,death,id,death,id,id,death,id,id));
      
      pool.start();
      
      ScheduledExecutorService scheduler =
          Executors.newScheduledThreadPool(1);
      
      scheduler.scheduleAtFixedRate(runPerge(pool), 0, 2, TimeUnit.SECONDS);
      
      System.in.read();
      
      scheduler.shutdownNow();


      server.stop();

    } catch (IOException | MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

  private static Runnable runPerge(final MinionPool pool) {
    return new Runnable() {
      @Override
      public void run() {
        pool.pergeZombies();
      }
      
    };
  }

  private static JMXConnectorServer createJmxConnectorServer(int port)
      throws IOException {
    LocateRegistry.createRegistry(port);
    MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    JMXServiceURL url = new JMXServiceURL(
        "service:jmx:rmi://localhost/jndi/rmi://localhost:" + port + "/jmxrmi");
    return JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbs);
  }
  
  private static void registerMXBean(MinionPool pool)
      throws MalformedObjectNameException, InstanceAlreadyExistsException,
      MBeanRegistrationException, NotCompliantMBeanException {
    MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

    ObjectName mxbeanName = new ObjectName(
        "org.pitest.minion:type=ControllerCommands");

    ControllerCommands mxbean = new ControllerCommands(pool);

    mbs.registerMBean(mxbean, mxbeanName);
  }
  
  
  private static int findPort() throws IOException {
    try (
        ServerSocket socket = new ServerSocket(0);
    ) {
      return socket.getLocalPort();

    }
  }
}
