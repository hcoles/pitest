package org.pitest.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.pitest.functional.SideEffect1;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;

/**
 * This class provides the workings necessary to connect to a running JVM and to
 * replace classes.
 * 
 * @author David A. Kavanagh <a href="mailto:dak@dotech.com">dak@dotech.com</a>
 */
public class HotSwap {
  private EventQueueMonitor eqm;
  private VirtualMachine    vm;

  public HotSwap() {
  }

  public void connect(final String name) throws Exception {
    connect(null, null, name);
  }

  public void connect(final String host, final String port) throws Exception {
    connect(host, port, null);
  }

  public JavaProcess launchVM(final Class<?> mainClass,
      final SideEffect1<String> sysoutHandler, final String args)
      throws IOException, IllegalConnectorArgumentsException, VMStartException {
    final VirtualMachineManager manager = Bootstrap.virtualMachineManager();
    // final Map<String, Argument> arg0 = new HashMap<String, Argument>();
    final Map<String, Argument> f = manager.defaultConnector()
        .defaultArguments();

    final String classpath = System.getProperty("java.class.path");
    f.get("main").setValue(
        "-cp " + classpath + " " + mainClass.getName() + " " + args);

    this.vm = manager.defaultConnector().launch(f);
    if (!this.vm.canRedefineClasses()) {
      throw new RuntimeException("JVM doesn't support class replacement");
    }
    this.eqm = new EventQueueMonitor(this.vm.eventQueue(), System.out);

    return new JavaProcess(this.vm.process(), sysoutHandler);

  }

  public void resume() {

    this.vm.resume();
  }

  public CountDownLatch setBreakPoint(final Class<?> clazz, final String method) {
    final List<ReferenceType> classes = this.vm.classesByName(clazz.getName());
    final CountDownLatch countDownLatch = new CountDownLatch(1);
    if (classes.isEmpty()) {
      final ClassPrepareRequest cpr = this.vm.eventRequestManager()
          .createClassPrepareRequest();
      // ReferenceType rt = vm.
      cpr.addClassFilter(clazz.getName());
      cpr.setSuspendPolicy(EventRequest.SUSPEND_ALL);

      final SideEffect1<Event> hook = new SideEffect1<Event>() {

        public void apply(final Event a) {
          final ClassPrepareEvent cpe = (ClassPrepareEvent) a;
          System.out.println("Ref type is " + cpe.referenceType());
          System.out.flush();

          setBreakPointOnLoadedClass(clazz, method, countDownLatch);
          a.virtualMachine().resume();

        }

      };

      this.eqm.addHook(cpr, hook);

      cpr.enable();
    } else {
      setBreakPointOnLoadedClass(clazz, method, countDownLatch);
    }

    return countDownLatch;
  }

  public void setBreakPointOnLoadedClass(final Class<?> clazz,
      final String method, final CountDownLatch countDownLatch) {
    final List<ReferenceType> refs = this.vm.classesByName(clazz.getName());
    final ReferenceType rt = refs.get(0);

    for (final Method each : rt.methodsByName(method)) {
      final BreakpointRequest bp = this.vm.eventRequestManager()
          .createBreakpointRequest(each.location());
      final SideEffect1<Event> hook = new SideEffect1<Event>() {

        public void apply(final Event a) {
          countDownLatch.countDown();
          a.request().disable();

        }

      };
      this.eqm.addHook(bp, hook);
      bp.enable();
      System.out.println("Enabled breakpoint at " + bp.location());
    }
  }

  public void prepareInitialHotSwap(final String name, final byte[] bytes) {

    // vm.allThreads().get(0).
    // vm.

    final ClassPrepareRequest cpr = this.vm.eventRequestManager()
        .createClassPrepareRequest();
    // ReferenceType rt = vm.
    cpr.addClassFilter(name);
    cpr.setSuspendPolicy(EventRequest.SUSPEND_ALL);

    final SideEffect1<Event> hook = new SideEffect1<Event>() {

      public void apply(final Event a) {
        final ClassPrepareEvent cpe = (ClassPrepareEvent) a;
        System.out.println("Ref type is " + cpe.referenceType());
        System.out.flush();

        hotSwapClassOnVMBreak(name, bytes);
        // a.virtualMachine().resume();

      }

    };

    this.eqm.addHook(cpr, hook);

    cpr.enable();

  }

  public void hotSwapClassOnVMBreak(final String name, final byte[] bytes) {
    final List<ReferenceType> refs = this.vm.classesByName(VMBreak.class
        .getName());
    if (refs.isEmpty()) {
      this.prepareInitialHotSwap(name, bytes);
    } else {

      final ReferenceType rt = refs.get(0);

      for (final Method each : rt.methodsByName("pause")) {
        final BreakpointRequest bp = this.vm.eventRequestManager()
            .createBreakpointRequest(each.location());
        final SideEffect1<Event> hook = new SideEffect1<Event>() {

          public void apply(final Event a) {

            try {

              // final List<ReferenceType> refs = vm.classesByName(VMBreak.class
              // .getName());
              // Field f = refs.get(0).fieldByName("test");
              // Value value = refs.get(0).getValue(f);

              // System.out.println("Value is " + value);

              replace(bytes, name);
            } catch (final Exception e) {
              Unchecked.translateCheckedException(e);
            }

            // a.virtualMachine().resume();
          }

        };

        this.eqm.addHook(bp, hook);
        bp.enable();
        System.out.println("Enabled breakpoint at " + bp.location());
      }

    }

  }

  // either host,port will be set, or name
  private void connect(final String host, final String port, final String name)
      throws Exception {
    // connect to JVM
    final boolean useSocket = (port != null);

    final VirtualMachineManager manager = Bootstrap.virtualMachineManager();
    final List<AttachingConnector> connectors = manager.attachingConnectors();
    AttachingConnector connector = null;
    // System.err.println("Connectors available");
    for (int i = 0; i < connectors.size(); i++) {
      final AttachingConnector tmp = connectors.get(i);
      // System.err.println("conn "+i+"  name="+tmp.name()+" transport="+tmp.transport().name()+
      // " description="+tmp.description());
      if (!useSocket && tmp.transport().name().equals("dt_shmem")) {
        connector = tmp;
        break;
      }
      if (useSocket && tmp.transport().name().equals("dt_socket")) {
        connector = tmp;
        break;
      }
    }
    if (connector == null) {
      throw new IllegalStateException("Cannot find shared memory connector");
    }

    final Map<String, Argument> args = connector.defaultArguments();
    Connector.Argument arg;
    // use name if using dt_shmem
    if (!useSocket) {
      arg = args.get("name");
      arg.setValue(name);
    }
    // use port if using dt_socket
    else {
      arg = args.get("port");
      arg.setValue(port);
    }
    this.vm = connector.attach(args);

    // query capabilities
    if (!this.vm.canRedefineClasses()) {
      throw new Exception("JVM doesn't support class replacement");
    }

  }

  public void replace(final byte[] classBytes, final String className)
      throws Exception {

    final List<ReferenceType> classes = this.vm.classesByName(className);

    // if the class isn't loaded on the VM, can't do the replace.
    if ((classes == null) || (classes.size() == 0)) {
      System.err.println("!!!!! Could not find " + className);
      return;
    }

    for (int i = 0; i < classes.size(); i++) {
      final ReferenceType refType = classes.get(i);
      final HashMap<ReferenceType, byte[]> map = new HashMap<ReferenceType, byte[]>();
      map.put(refType, classBytes);
      System.err.println(">>>>>>>>>>> !Hotswapping class " + className);
      System.err.flush();
      this.vm.redefineClasses(map);
    }

  }

  public void disconnect() throws Exception {
    // nothing to do here?
  }

}
