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
package org.pitest.process;

import static org.pitest.functional.prelude.Prelude.or;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.pitest.functional.FCollection;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.Option;
import org.pitest.functional.SideEffect1;
import org.pitest.functional.predicate.Predicate;
import org.pitest.util.Monitor;
import org.pitest.util.StreamMonitor;

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

    addPITJavaAgent(agentJarLocator, cmd);
    addLaunchJavaAgents(cmd);

    cmd.add(mainClass.getName());
    cmd.addAll(programArgs);
    return cmd;
  }

  private static void addPITJavaAgent(final JavaAgent agentJarLocator,
      final List<String> cmd) {
    final Option<String> jarLocation = agentJarLocator.getJarLocation();
    for (final String each : jarLocation) {
      cmd.add("-javaagent:" + each);
    }
  }

  private static void addLaunchJavaAgents(final List<String> cmd) {
    final RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();
    @SuppressWarnings("unchecked")
    final FunctionalList<String> agents = FCollection.filter(
        rt.getInputArguments(), or(isJavaAgentParam(), isEnvironmentSetting()));
    cmd.addAll(agents);
  }

  private static Predicate<String> isEnvironmentSetting() {
    return new Predicate<String>() {
      public Boolean apply(final String a) {
        return a.startsWith("-D");
      }
    };
  }

  private static Predicate<String> isJavaAgentParam() {
    return new Predicate<String>() {

      public Boolean apply(final String a) {
        return a.toLowerCase().startsWith("-javaagent");
      }

    };
  }

  public static JavaProcess launch(final File workingDirectory,
      final String javaProc, final SideEffect1<String> systemOutHandler,
      final SideEffect1<String> sysErrHandler, final List<String> args,
      final Class<?> mainClass, final List<String> programArgs,
      final JavaAgent javaAgent, final String initialClassPath)
      throws IOException {

    final List<String> cmd = createLaunchArgs(javaProc, javaAgent, args,
        mainClass, programArgs);
    final ProcessBuilder processBuilder = new ProcessBuilder(cmd);
    processBuilder.directory(workingDirectory);
    final Map<String, String> env = processBuilder.environment();

    env.put("CLASSPATH", initialClassPath);
    final Process process = processBuilder.start();

    return new JavaProcess(process, systemOutHandler, sysErrHandler);
  }

}
