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
package org.pitest.mutationtest.hotswap;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.apache.bcel.classfile.JavaClass;
import org.pitest.functional.SideEffect1;
import org.pitest.internal.ClassPath;
import org.pitest.internal.IsolationUtils;
import org.pitest.mutationtest.AbstractMutationTestUnit;
import org.pitest.util.Debugger;
import org.pitest.util.JavaProcess;

import com.reeltwo.jumble.mutation.Mutater;
import com.sun.jdi.event.Event;

public class HotSwapWorker {

  private static class Hook implements SideEffect1<Event> {

    private final Debugger debugger;
    private int            endMutation;
    private int            currentMutation;
    private String         classToMutate;
    private Mutater        m;

    public Hook(final Debugger debugger) {
      this.debugger = debugger;
    }

    public void apply(final Event a) {
      this.m.setMutationPoint(this.currentMutation);
      JavaClass activeMutation;
      try {
        activeMutation = this.m.jumbler(this.classToMutate);
        this.debugger.hotSwapClass(activeMutation.getBytes(),
            this.classToMutate);
      } catch (final ClassNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      // make sure we increment the mutation before we resume in case
      // of a race
      if (this.currentMutation <= this.endMutation) {
        this.currentMutation++;
        this.debugger.resume();
      } else {
        this.currentMutation++;
      }

    }

    public Debugger getDebugger() {
      return this.debugger;
    }

  }

  private final JavaProcess process;
  private final Hook        hook;
  private final File        inputfile;
  private final File        result;
  private volatile boolean  runComplete;

  HotSwapWorker(final Debugger debugger, final ClassPath cp) throws IOException {
    this.inputfile = File.createTempFile(AbstractMutationTestUnit
        .randomFilename(), ".data");
    this.result = File.createTempFile(
        AbstractMutationTestUnit.randomFilename(), ".results");
    this.hook = new Hook(debugger);

    final SideEffect1<String> soh = new SideEffect1<String>() {
      public void apply(final String a) {
        System.out.println(a);
      }
    };

    final SideEffect1<String> seh = new SideEffect1<String>() {
      public void apply(final String a) {
        System.err.println(a);
      }
    };

    final String[] args = { this.inputfile.getAbsolutePath(),
        this.result.getAbsolutePath(), IsolationUtils.toTransportString(cp) };

    this.process = JavaProcess.launch(debugger, soh, seh, Collections
        .<String> emptyList(), HotSwapMutationTestSlave.class, Arrays
        .asList(args));
    debugger.setBreakPoint(HotSwapMutationTestSlave.class, "receiveMutation",
        this.hook);
    final SideEffect1<Event> doneHook = new SideEffect1<Event>() {

      public void apply(final Event a) {
        markRunComplete();
      }

    };
    debugger.setBreakPoint(HotSwapMutationTestSlave.class, "waitForInput",
        doneHook);
    debugger.resume();
  }

  protected void markRunComplete() {
    this.runComplete = true;
  }

  public void reset(final Class<?> classToMutate, final Mutater m,
      final int start, final int end) {
    this.runComplete = false;
    this.hook.classToMutate = classToMutate.getName();
    this.hook.m = m;
    this.hook.currentMutation = start;
    this.hook.endMutation = end;
  }

  public JavaProcess getProcess() {
    return this.process;
  }

  public Hook getHook() {
    return this.hook;
  }

  public File getInputfile() {
    return this.inputfile;
  }

  public File getResult() {
    return this.result;
  }

  public Debugger getDebugger() {
    return this.hook.getDebugger();
  }

  public boolean isRunComplete() {
    return this.runComplete;
  }

  public boolean waitForRunToComplete(final long timeout) {
    final long t0 = System.currentTimeMillis();
    while (!isRunComplete()) {
      try {
        Thread.sleep(1);
        if (System.currentTimeMillis() - t0 > timeout) {
          return false;
        }
      } catch (final InterruptedException e) {
        e.printStackTrace();
      }
    }
    return true;

  }

}
