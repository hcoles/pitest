/*
 * Copyright 2010 Henry Coles
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and limitations under the License. 
 */
package org.pitest.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pitest.functional.SideEffect1;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;

public class DefaultDebugger implements Debugger {

  private EventQueueMonitor eqm;
  private VirtualMachine    vm;

  public void hotSwapClass(final byte[] classBytes, final String className) {

    final List<ReferenceType> classes = this.vm.classesByName(className);
    if (classes.isEmpty()) {

      final SideEffect1<Event> loadHook = new SideEffect1<Event>() {

        public void apply(final Event a) {

          hotSwapLoadedClass(classBytes, className);
          a.virtualMachine().resume();

        }

      };

      addClassLoadHook(className, loadHook);

    } else {
      hotSwapLoadedClass(classBytes, className);
    }

  }

  public void addClassLoadHook(final String className,
      final SideEffect1<Event> event) {
    final ClassPrepareRequest cpr = this.vm.eventRequestManager()
        .createClassPrepareRequest();
    // ReferenceType rt = vm.
    cpr.addClassFilter(className);
    cpr.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);

    this.eqm.addHook(cpr, event);

    cpr.enable();
  }

  public void hotSwapLoadedClass(final byte[] classBytes, final String className) {

    final List<ReferenceType> classes = this.vm.classesByName(className);

    // if the class isn't loaded on the VM, can't do the replace.
    if ((classes == null) || (classes.size() == 0)) {
      throw new RuntimeException("Could not find " + className
          + " to set a breakpoint");
    }

    for (int i = 0; i < classes.size(); i++) {
      final ReferenceType refType = classes.get(i);
      final HashMap<ReferenceType, byte[]> map = new HashMap<ReferenceType, byte[]>();
      map.put(refType, classBytes);

      this.vm.redefineClasses(map);

    }

  }

  public void setBreakPoint(final Class<?> clazz, final String method,
      final SideEffect1<Event> hook) {
    final List<ReferenceType> classes = this.vm.classesByName(clazz.getName());
    if (classes.isEmpty()) {

      final SideEffect1<Event> loadHook = new SideEffect1<Event>() {

        public void apply(final Event a) {

          setBreakPointOnLoadedClass(clazz, method, hook);
          a.virtualMachine().resume();

        }

      };

      addClassLoadHook(clazz.getName(), loadHook);

    } else {
      setBreakPointOnLoadedClass(clazz, method, hook);
    }

    // return countDownLatch;
  }

  public void setBreakPointOnLoadedClass(final Class<?> clazz,
      final String method, final SideEffect1<Event> hook) {
    final List<ReferenceType> refs = this.vm.classesByName(clazz.getName());
    final ReferenceType rt = refs.get(0);

    for (final Method each : rt.methodsByName(method)) {
      final BreakpointRequest bp = this.vm.eventRequestManager()
          .createBreakpointRequest(each.location());
      this.eqm.addHook(bp, hook);
      bp.enable();
    }
  }

  public void resume() {
    this.vm.resume();
  }

  public Process launchProcess(final List<String> args) {
    final VirtualMachineManager manager = Bootstrap.virtualMachineManager();
    // final Map<String, Argument> arg0 = new HashMap<String, Argument>();
    final Map<String, Argument> f = manager.defaultConnector()
        .defaultArguments();

    final String argString = listToString(args);
    f.get("main").setValue(argString);

    try {

      // TODO can we get a performance gain by tweaking parameters?
      this.vm = manager.defaultConnector().launch(f);
      if (!this.vm.canRedefineClasses()) {
        throw new RuntimeException("JVM doesn't support class replacement");
      }
      this.eqm = new EventQueueMonitor(this.vm.eventQueue());

      return this.vm.process();
    } catch (final IllegalConnectorArgumentsException e) {
      throw Unchecked.translateCheckedException(e);
    } catch (final VMStartException e) {
      throw Unchecked.translateCheckedException(e);
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

  private String listToString(final List<String> args) {
    final StringBuffer sb = new StringBuffer();
    for (final String each : args) {
      sb.append(each + " ");
    }
    return sb.toString();
  }

}
