package org.pitest.controller;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.rmi.registry.LocateRegistry;
import java.util.Arrays;
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

import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.coverage.TestInfo;
import org.pitest.functional.Option;
import org.pitest.functional.predicate.False;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.engine.gregor.config.GregorEngineFactory;
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
      
      
      GregorEngineFactory eng = new GregorEngineFactory();
      MutationEngine engine = eng.createEngine(False.<String>instance(), null);
      
      Mutater m = engine.createMutator(ClassloaderByteArraySource.fromContext());
      
      Collection<MutationDetails> toDo = m.findMutations(ClassName.fromClass(Controller.class));
      TestInfo one = new TestInfo("one", "m", 1, Option.<ClassName>none(), 2);
      TestInfo two = new TestInfo("two", "m", 1, Option.<ClassName>none(), 2);      
      for (MutationDetails i : toDo ) {
        i.addTestsInOrder(Arrays.asList(one,two));
      }
      
      WorkScheduler workScheduler =  new DeafultWorkScheduler(toDo, sysOutListener());
      MinionPool pool = new MinionPool(2, new MinionFactory(myPort), workScheduler);
      registerMXBean(pool);

      
      pool.start();
      
      ScheduledExecutorService scheduler =
          Executors.newScheduledThreadPool(1);
      
      scheduler.scheduleAtFixedRate(runPerge(pool), 0, 2, TimeUnit.SECONDS);
      
     
      workScheduler.awaitCompletion(); 
      
      scheduler.shutdownNow();


      server.stop();

    } catch (IOException | MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

  private static ResultListener sysOutListener() {
    return new ResultListener() {

      @Override
      public void report(MutationResult r) {
       System.out.println(r);
        
      }
      
    };
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
