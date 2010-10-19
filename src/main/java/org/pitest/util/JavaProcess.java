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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.pitest.functional.SideEffect1;

public class JavaProcess {

  private final Process       process;

  private final StreamMonitor out;
  private final StreamMonitor err;

  public JavaProcess(final Process process,
      final SideEffect1<String> sysoutHandler,
      final SideEffect1<String> syserrHandler) {
    this.process = process;
    this.out = new StreamMonitor(process.getInputStream(), sysoutHandler);
    this.err = new StreamMonitor(process.getErrorStream(), syserrHandler);

  }

  public void destroy() {
    this.process.destroy();
  }

  public int waitToDie() throws InterruptedException {
    final int exitVal = this.process.waitFor();
    this.out.requestStop();
    this.err.requestStop();
    return exitVal;
  }

  public boolean isAlive() {
    try {
      this.process.exitValue();
      return false;
    } catch (final IllegalThreadStateException e) {
      return true;
    }
  }

  public OutputStream stdIn() {
    return this.process.getOutputStream();
  }

  public static JavaProcess launch(final Debugger debugger,
      final SideEffect1<String> systemOutHandler,
      final SideEffect1<String> sysErrHandler, final List<String> args,
      final Class<?> mainClass, final List<String> programArgs)
      throws IOException {

    final List<String> cmd = createLaunchArgs("", args, mainClass, programArgs);
    return new JavaProcess(debugger.launchProcess(cmd), systemOutHandler,
        sysErrHandler);

  }

  private static List<String> createLaunchArgs(final String javaProcess,
      final List<String> args, final Class<?> mainClass,
      final List<String> programArgs) {

    final String classpath = System.getProperty("java.class.path");

    final List<String> cmd = new ArrayList<String>();
    cmd.addAll(Arrays.asList(javaProcess, "-cp", classpath));
    cmd.addAll(args);
    cmd.add(mainClass.getName());
    cmd.addAll(programArgs);
    return cmd;
  }

  public static JavaProcess launch(final SideEffect1<String> systemOutHandler,
      final SideEffect1<String> sysErrHandler, final List<String> args,
      final Class<?> mainClass, final List<String> programArgs)
      throws IOException {
    final String separator = System.getProperty("file.separator");
    final String javaProc = System.getProperty("java.home") + separator + "bin"
        + separator + "java";
    final List<String> cmd = createLaunchArgs(javaProc, args, mainClass,
        programArgs);
    final ProcessBuilder processBuilder = new ProcessBuilder(cmd);
    final Process process = processBuilder.start();

    return new JavaProcess(process, systemOutHandler, sysErrHandler);
  }

  public static JavaProcess launch(final List<String> args,
      final Class<?> mainClass, final List<String> programArgs)
      throws IOException {

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

    return launch(soh, seh, args, mainClass, programArgs);
  }

}
