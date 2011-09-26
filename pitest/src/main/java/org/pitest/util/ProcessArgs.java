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
package org.pitest.util;

import static org.pitest.functional.Prelude.print;
import static org.pitest.functional.Prelude.printTo;

import java.util.Collections;
import java.util.List;

import org.pitest.functional.SideEffect1;
import org.pitest.internal.ClassPath;

public class ProcessArgs {

  private final String        launchClassPath;
  private SideEffect1<String> stdout  = print(String.class);
  private SideEffect1<String> stdErr  = printTo(String.class, System.err);
  private List<String>        jvmArgs = Collections.emptyList();
  private JavaAgent           javaAgentFinder;

  private ProcessArgs(final String launchClassPath) {
    this.launchClassPath = launchClassPath;
  }

  public static ProcessArgs withClassPath(final String cp) {
    return new ProcessArgs(cp);
  }

  public static ProcessArgs withClassPath(final ClassPath cp) {
    return new ProcessArgs(cp.getLocalClassPath());
  }

  public ProcessArgs andStdout(final SideEffect1<String> stdout) {
    this.stdout = stdout;
    return this;
  }

  public ProcessArgs andStderr(final SideEffect1<String> stderr) {
    this.stdErr = stderr;
    return this;
  }

  public ProcessArgs andJVMArgs(final List<String> jvmArgs) {
    this.jvmArgs = jvmArgs;
    return this;
  }

  public ProcessArgs andJavaAgentFinder(final JavaAgent agent) {
    this.javaAgentFinder = agent;
    return this;
  }

  public String getLaunchClassPath() {
    return this.launchClassPath;
  }

  public SideEffect1<String> getStdout() {
    return this.stdout;
  }

  public SideEffect1<String> getStdErr() {
    return this.stdErr;
  }

  public List<String> getJvmArgs() {
    return this.jvmArgs;
  }

  public JavaAgent getJavaAgentFinder() {
    return this.javaAgentFinder;
  }

  public void setStdout(final SideEffect1<String> stdout) {
    this.stdout = stdout;
  }

  public void setStdErr(final SideEffect1<String> stdErr) {
    this.stdErr = stdErr;
  }

  public void setJvmArgs(final List<String> jvmArgs) {
    this.jvmArgs = jvmArgs;
  }

  public void setJavaAgentFinder(final JavaAgent javaAgentFinder) {
    this.javaAgentFinder = javaAgentFinder;
  }

};
