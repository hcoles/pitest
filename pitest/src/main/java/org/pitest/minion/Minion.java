package org.pitest.minion;

import static org.pitest.util.Unchecked.translateCheckedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.pitest.boot.HotSwapAgent;
import org.pitest.classinfo.CachingByteArraySource;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.functional.F;
import org.pitest.functional.F3;
import org.pitest.functional.FCollection;
import org.pitest.functional.predicate.False;
import org.pitest.functional.prelude.Prelude;
import org.pitest.minion.commands.Command;
import org.pitest.minion.commands.MutId;
import org.pitest.minion.commands.Status;
import org.pitest.minion.commands.Test;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationEngineFactory;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.config.ClientPluginServices;
import org.pitest.mutationtest.config.MinionSettings;
import org.pitest.mutationtest.config.TestPluginArguments;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MethodName;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.execute.CheckTestHasFailedResultListener;
import org.pitest.mutationtest.execute.HotSwap;
import org.pitest.mutationtest.mocksupport.BendJavassistToMyWillTransformer;
import org.pitest.mutationtest.mocksupport.JavassistInputStreamInterceptorAdapater;
import org.pitest.mutationtest.mocksupport.JavassistInterceptor;
import org.pitest.testapi.Configuration;
import org.pitest.testapi.TestResult;
import org.pitest.testapi.TestUnit;
import org.pitest.testapi.execute.Container;
import org.pitest.testapi.execute.ExitingResultCollector;
import org.pitest.testapi.execute.FindTestUnits;
import org.pitest.testapi.execute.MultipleTestGroup;
import org.pitest.testapi.execute.Pitest;
import org.pitest.testapi.execute.containers.ConcreteResultCollector;
import org.pitest.testapi.execute.containers.UnContainer;
import org.pitest.util.Glob;
import org.pitest.util.IsolationUtils;
import org.pitest.util.Unchecked;

public class Minion {
  
  // We maintain a small cache to avoid reading byte code off disk more than once
  // Size is arbitrary but assumed to be large enough to cover likely max number of inner classes
  private static final int CACHE_SIZE = 12;

  public static void main(String[] args) {
    int controllerPort = Integer.parseInt(args[0]);
    String name = args[1];
    String testPluginName = args[2];
    String engineName = args[3];
    
    enablePowerMockSupport();
    
    ClientPluginServices plugins = new ClientPluginServices(IsolationUtils.getContextClassLoader());
    MinionSettings settings = new MinionSettings(plugins);
    
    final ClassByteArraySource byteSource = new CachingByteArraySource(new ClassloaderByteArraySource(
        IsolationUtils.getContextClassLoader()), CACHE_SIZE);
    
    
    MutationEngineFactory ef = settings.createEngine(engineName);
    MutationEngine engine = ef.createEngine(False.<String>instance(), null);
    Mutater mutator = engine.createMutator(byteSource);
    
    Configuration testPlugin = settings.getTestFrameworkPlugin(TestPluginArguments.defaults().withTestPlugin(testPluginName), byteSource);
    ControllerCommandsMXBean controller = connectToController(controllerPort);
    final F3<ClassName, ClassLoader, byte[], Boolean> hotswap = new HotSwap(byteSource);
    TestSource tests = new TestSource(testPlugin);
    
    MinionWorker worker = new MinionWorker(name, hotswap, mutator, tests, controller);
    
    worker.run();
        
    System.out.println("bye bye " + name);
  }

  private static ControllerCommandsMXBean connectToController(int controllerPort) {
    JMXServiceURL url;
    try {
      url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://:" + controllerPort + "/jmxrmi");
      JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
      MBeanServerConnection connection = jmxc.getMBeanServerConnection();
      
      ControllerCommandsMXBean mbeanProxy = JMX.newMXBeanProxy(connection, ObjectName.getInstance("org.pitest.minion:type=ControllerCommands"), 
          ControllerCommandsMXBean.class, true);
      
      return mbeanProxy;
      

    } catch (IOException | NullPointerException | MalformedObjectNameException e) {
      throw Unchecked.translateCheckedException(e);
    }
  
    
  }
  
  private static void enablePowerMockSupport() {
    // Bwahahahahahahaha
    HotSwapAgent.addTransformer(new BendJavassistToMyWillTransformer(Prelude
        .or(new Glob("javassist/*")), JavassistInputStreamInterceptorAdapater.inputStreamAdapterSupplier(JavassistInterceptor.class)));
  }


}


class MinionWorker {
  private final String name;
  private final Mutater engine;
  private final TestSource testPlugin;
  private final ControllerCommandsMXBean controller;
  private final F3<ClassName, ClassLoader, byte[], Boolean> hotswap;
  ClassLoader loader = IsolationUtils.getContextClassLoader();
  

  public MinionWorker(String name, F3<ClassName, ClassLoader, byte[], Boolean> hotswap, Mutater engine, TestSource testPlugin, ControllerCommandsMXBean controller) {
    this.name = name;
    this.engine = engine;
    this.testPlugin = testPlugin;
    this.controller = controller;
    this.hotswap = hotswap;
  }

  public void run() {
    try {
      
      System.out.println("Waiting...");
      
      controller.hello(name);

      boolean run = true;
      while (run) {
        System.out.println(name + " is polling");
        Command work = controller.pull(name);
        switch (work.getAction()) {
        case DIE :
          System.out.println(name + " will die");
          run = false;
          controller.report(name, Status.ACK);
          break;
        case ANALYSE :
          analyseMutant(work);
          break;
        case SELFCHECK :
          
        }
      }
             
      
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } 
    
  }

  private void analyseMutant(Command work) throws ClassNotFoundException {
    System.out.println(name + " is doing " + work);
    
    MutationIdentifier id = toIdentifier(work.getId());
    Mutant mutant = engine.getMutation(id);
    boolean ok = this.hotswap.apply(mutant.getDetails().getClassName(), loader, mutant.getBytes());
    if (!ok) {
      System.out.println("Couldn't load mutant. Do something");
    }
    
    
    Test test = work.getTest();          
    Class<?> clazz = Class.forName(test.getClazz());
    TestUnit t = testPlugin.test(clazz, test.getName());
    MutationStatusTestPair result = doTestsDetectMutation(createNewContainer(),Collections.singletonList(t));
    Status s = resultToStatus(result);
    
    controller.report(name, s);
  }
  
  private MutationIdentifier toIdentifier(MutId id) {
    Location loc = Location.location(ClassName.fromString(id.getClazz()), 
        MethodName.fromString(id.getMethod()),
        id.getMethodDesc());
    return new MutationIdentifier(loc, id.getIndex(), id.getOperator());
  }

  private Status resultToStatus(MutationStatusTestPair result) {
    System.out.println("Status was " + result.getStatus());
    if (result.getStatus().equals(DetectionStatus.SURVIVED)) {
      return Status.TEST_PASSED;
    }
    return Status.TEST_FAILED;
  }

  private static Container createNewContainer() {
    final Container c = new UnContainer() {
      @Override
      public List<TestResult> execute(final TestUnit group) {
        List<TestResult> results = new ArrayList<>();
        final ExitingResultCollector rc = new ExitingResultCollector(
            new ConcreteResultCollector(results));
        group.execute(rc);
        return results;
      }
    };
    return c;
  }
  
  private MutationStatusTestPair doTestsDetectMutation(final Container c,
      final List<TestUnit> tests) {
    try {
      final CheckTestHasFailedResultListener listener = new CheckTestHasFailedResultListener();

      final Pitest pit = new Pitest(listener);
      pit.run(c, createEarlyExitTestGroup(tests));

      return createStatusTestPair(listener);
    } catch (final Exception ex) {
      throw translateCheckedException(ex);
    }

  }
  
  private MutationStatusTestPair createStatusTestPair(
      final CheckTestHasFailedResultListener listener) {
    if (listener.lastFailingTest().hasSome()) {
      return new MutationStatusTestPair(listener.getNumberOfTestsRun(),
          listener.status(), listener.lastFailingTest().value()
              .getQualifiedName());
    } else {
      return new MutationStatusTestPair(listener.getNumberOfTestsRun(),
          listener.status());
    }
  }

  private List<TestUnit> createEarlyExitTestGroup(final List<TestUnit> tests) {
    return Collections.<TestUnit> singletonList(new MultipleTestGroup(tests));
  }


  
}


class TestSource {
  private final FindTestUnits finder;
  
  TestSource(final Configuration config) {
    finder = new FindTestUnits(config);
  }
  
  TestUnit test(Class<?> clazz, String name) {
    // FIXME - hugely inefficient
    Iterable<Class<?>> search = Arrays.<Class<?>>asList(clazz);
    List<TestUnit> allTests = finder.findTestUnitsForAllSuppliedClasses(search);
    System.out.println(allTests.size());
    return FCollection.findFirst(allTests, named(name)).value();
  }

  private F<TestUnit, Boolean> named(final String name) {
    return new  F<TestUnit, Boolean> () {
      @Override
      public Boolean apply(TestUnit a) {
        String qualifiedName = a.getDescription().getQualifiedName();
        System.out.println(qualifiedName + "/" + name);
        return qualifiedName.contentEquals(name);
      }
      
    };
  }
  
}
