package org.pitest.controller;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.rmi.registry.LocateRegistry;
import java.util.Collection;
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

import org.pitest.mutationtest.config.TestPluginArguments;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.process.LaunchOptions;
import org.pitest.util.Unchecked;

public class Controller {

  private final String                classPath;
  private final File                  baseDir;
  private final TestPluginArguments   pitConfig;
  private final String                mutationEngine;
  private final boolean               verbose;
  private final LaunchOptions         config;

  
  
  
  public Controller(String classPath, File baseDir,
      TestPluginArguments pitConfig, String mutationEngine, boolean verbose,
      LaunchOptions config) {
    super();
    this.classPath = classPath;
    this.baseDir = baseDir;
    this.pitConfig = pitConfig;
    this.mutationEngine = mutationEngine;
    this.verbose = verbose;
    this.config = config;
  }

  public void process(Collection<MutationDetails> mutations, ResultListener listener) {

    try {
      // slight chance another process will steal the port before we use it
      // but we only do this once
      int myPort = findPort();
      System.out.println("Controller on " + myPort);
      JMXConnectorServer server = createJmxConnectorServer(myPort);
      server.start();
      
      
      WorkScheduler workScheduler =  new DeafultWorkScheduler(mutations, listener);
      MinionPool pool = new MinionPool(2, new MinionFactory(myPort, classPath, baseDir, pitConfig, mutationEngine, verbose, config), workScheduler);
      registerMXBean(pool);

      
      pool.start();
      
      ScheduledExecutorService scheduler =
          Executors.newScheduledThreadPool(1);
      
      scheduler.scheduleAtFixedRate(runPerge(pool), 0, 2, TimeUnit.SECONDS);
      
     
      workScheduler.awaitCompletion(); 
      
      scheduler.shutdownNow();

      server.stop();
      
      
    } catch (Exception e) {
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
