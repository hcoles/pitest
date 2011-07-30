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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.pitest.functional.Option;
import org.pitest.functional.SideEffect1;

public class JavaProcess {

  private final Process process;

  private final Monitor out;
  private final Monitor err;

  public JavaProcess(final Process process,
      final SideEffect1<String> sysoutHandler,
      final SideEffect1<String> syserrHandler) {
    this.process = process;
    this.out = new StreamMonitor(process.getInputStream(), sysoutHandler);
    this.err = new StreamMonitor(process.getErrorStream(), syserrHandler);
    this.out.requestStart();
    this.err.requestStart();

  }

  public void destroy() {
    this.out.requestStop();
    this.err.requestStop();
    this.process.destroy();
  }

  public void cleanup() {
    try {
      this.process.getErrorStream().close();
      this.process.getOutputStream().close();
      this.process.getInputStream().close();
    } catch (final IOException ex) {
      ex.printStackTrace();
    }
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

  private static List<String> createLaunchArgs(final String javaProcess,
      final JavaAgent agentJarLocator, final List<String> args,
      final Class<?> mainClass, final List<String> programArgs) {

    final List<String> cmd = new ArrayList<String>();
    cmd.add(javaProcess);
    cmd.addAll(args);
    final Option<String> jarLocation = agentJarLocator.getJarLocation();
    for (final String each : jarLocation) {
      cmd.add("-javaagent:" + each);
    }

    cmd.add(mainClass.getName());
    cmd.addAll(programArgs);
    return cmd;
  }

  public static JavaProcess launch(final SideEffect1<String> systemOutHandler,
      final SideEffect1<String> sysErrHandler, final List<String> args,
      final Class<?> mainClass, final List<String> programArgs,
      final JavaAgent javaAgent, final String initialClassPath)
      throws IOException {
    final String separator = System.getProperty("file.separator");
    final String javaProc = System.getProperty("java.home") + separator + "bin"
        + separator + "java";
    final List<String> cmd = createLaunchArgs(javaProc, javaAgent, args,
        mainClass, programArgs);
    final ProcessBuilder processBuilder = new ProcessBuilder(cmd);
    final Map<String, String> env = processBuilder.environment();

    env.put("CLASSPATH", initialClassPath);
    final Process process = processBuilder.start();

    return new JavaProcess(process, systemOutHandler, sysErrHandler);
  }

  public static JavaProcess launch(final List<String> args,
      final Class<?> mainClass, final List<String> programArgs,
      final JavaAgent javaAgent) throws IOException {
    final String classpath = System.getProperty("java.class.path");
    return launch(args, mainClass, programArgs, javaAgent, classpath);
  }

  public static JavaProcess launch(final List<String> args,
      final Class<?> mainClass, final List<String> programArgs,
      final JavaAgent javaAgent, final String launchClassPath)
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

    return launch(soh, seh, args, mainClass, programArgs, javaAgent,
        launchClassPath);
  }

}
