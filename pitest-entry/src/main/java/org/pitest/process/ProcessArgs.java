/*
 * Copyright 2011 Henry Coles
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

import static org.pitest.functional.prelude.Prelude.println;
import static org.pitest.functional.prelude.Prelude.printlnTo;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.pitest.classpath.ClassPath;

public final class ProcessArgs {

  private final String        launchClassPath;
  private Consumer<String> stdout     = println(String.class);
  private Consumer<String> stdErr     = printlnTo(String.class, System.err);
  private List<String>        jvmArgs    = Collections.emptyList();
  private JavaAgent           javaAgentFinder;
  private File                workingDir = null;
  private String              javaExecutable;
  private Map<String, String> environmentVariables;
  private boolean             useClasspathJar = false;

  private ProcessArgs(final String launchClassPath) {
    this.launchClassPath = launchClassPath;
  }

  public static ProcessArgs withClassPath(final String cp) {
    return new ProcessArgs(cp);
  }

  public static ProcessArgs withClassPath(final ClassPath cp) {
    return new ProcessArgs(cp.getLocalClassPath());
  }

  public ProcessArgs andBaseDir(final File baseDir) {
    this.workingDir = baseDir;
    return this;
  }

  public ProcessArgs andStdout(final Consumer<String> stdout) {
    this.stdout = stdout;
    return this;
  }

  public ProcessArgs andStderr(final Consumer<String> stderr) {
    this.stdErr = stderr;
    return this;
  }
  
  public String getLaunchClassPath() {
    return this.launchClassPath;
  }

  public Consumer<String> getStdout() {
    return this.stdout;
  }

  public Consumer<String> getStdErr() {
    return this.stdErr;
  }

  public List<String> getJvmArgs() {
    return this.jvmArgs;
  }

  public JavaAgent getJavaAgentFinder() {
    return this.javaAgentFinder;
  }

  public File getWorkingDir() {
    return this.workingDir;
  }

  public String getJavaExecutable() {
    return this.javaExecutable;
  }

  public boolean useClasspathJar() {
    return useClasspathJar;
  }
  
  public ProcessArgs andLaunchOptions(final LaunchOptions launchOptions) {
    this.jvmArgs = launchOptions.getChildJVMArgs();
    this.javaAgentFinder = launchOptions.getJavaAgentFinder();
    this.javaExecutable = launchOptions.getJavaExecutable();
    this.environmentVariables = launchOptions.getEnvironmentVariables();
    this.useClasspathJar = launchOptions.useClasspathJar();
    return this;
  }

  public Map<String, String> getEnvironmentVariables() {
    return this.environmentVariables;
  }
}
