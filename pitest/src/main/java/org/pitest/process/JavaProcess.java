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

import org.pitest.functional.FCollection;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.Option;
import org.pitest.functional.SideEffect1;
import org.pitest.functional.predicate.Predicate;
import org.pitest.util.Monitor;
import org.pitest.util.StreamMonitor;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.pitest.functional.prelude.Prelude.or;

public class JavaProcess {

  private final Process process;
  private final Monitor out;
  private final Monitor err;

  public JavaProcess( Process process,
                      SideEffect1<String> sysoutHandler,
                      SideEffect1<String> syserrHandler) {
    this.process = process;

    out = new StreamMonitor(process.getInputStream(), sysoutHandler);
    err = new StreamMonitor(process.getErrorStream(), syserrHandler);
    out.requestStart();
    err.requestStart();

  }

  public void destroy() {
    out.requestStop();
    err.requestStop();
    process.destroy();
  }

  public int waitToDie() throws InterruptedException {
    int exitVal = process.waitFor();
    out.requestStop();
    err.requestStop();
    return exitVal;
  }

  public boolean isAlive() {
    try {
      process.exitValue();
      return false;
    } catch (IllegalThreadStateException e) {
      return true;
    }
  }

  private static List<String> createLaunchArgs( String javaProcess,
                                                JavaAgent agentJarLocator,
                                                List<String> args,
                                                Class<?> mainClass,
                                                List<String> programArgs) {

    List<String> cmd = new ArrayList<String>();
    cmd.add(javaProcess);
    cmd.addAll(args);

    addPITJavaAgent(agentJarLocator, cmd);
    addLaunchJavaAgents(cmd);

    cmd.add(mainClass.getName());
    cmd.addAll(programArgs);
    return cmd;
  }

  private static void addPITJavaAgent(JavaAgent agentJarLocator, List<String> cmd) {
    Option<String> jarLocation = agentJarLocator.getJarLocation();
    for (String each : jarLocation) {
      cmd.add("-javaagent:" + each);
    }
  }

  private static void addLaunchJavaAgents(List<String> cmd) {
    RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();
    @SuppressWarnings("unchecked")
    FunctionalList<String> agents = FCollection.filter(
        rt.getInputArguments(), or(isJavaAgentParam(), isEnvironmentSetting()));
    cmd.addAll(agents);
  }

  private static Predicate<String> isEnvironmentSetting() {
    return new Predicate<String>() {
      public Boolean apply(String a) {
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

  public static JavaProcess launch(File workingDirectory,
                                   String javaProc,
                                   SideEffect1<String> systemOutHandler,
                                   SideEffect1<String> sysErrHandler,
                                   List<String> args,
                                   Class<?> mainClass,
                                   List<String> programArgs,
                                   JavaAgent javaAgent,
                                   String initialClassPath)
      throws IOException {

    List<String> cmd = createLaunchArgs(javaProc, javaAgent, args,
        mainClass, programArgs);

    // IBM jdk adds this, thereby breaking everything
    removeClassPathProperties(cmd);

    ProcessBuilder processBuilder = new ProcessBuilder(cmd);
    processBuilder.directory(workingDirectory);

    Map<String, String> env = processBuilder.environment();
    env.put("CLASSPATH", initialClassPath);

    Process process = processBuilder.start();
    return new JavaProcess(process, systemOutHandler, sysErrHandler);
  }

  private static void removeClassPathProperties(List<String> cmd) {
    for (int i = cmd.size() - 1; i >= 0; i--) {
      if (cmd.get(i).startsWith("-Djava.class.path")) {
        cmd.remove(i);
      }
    }
  }

}
