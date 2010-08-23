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

import java.io.OutputStream;

import org.pitest.functional.SideEffect1;

public class JavaProcess {

  private final Process       process;
  private final StreamMonitor out;
  private final StreamMonitor err;

  // private final Class<?> mainClass;

  // private final ClassPath classPath;

  public JavaProcess(final Process process,
      final SideEffect1<String> sysoutHandler) {
    this.process = process;
    this.out = new StreamMonitor(process.getInputStream(), sysoutHandler);
    this.err = new StreamMonitor(process.getErrorStream(), System.err);
    // this.classPath = classPath;
  }

  public void waitToDie() throws InterruptedException {
    this.process.waitFor();
  }

  public OutputStream stdIn() {
    return this.process.getOutputStream();
  }

  // public void start() throws Exception {
  //
  // final String separator = System.getProperty("file.separator");
  // final String classpath = System.getProperty("java.class.path");
  // final String path = System.getProperty("java.home") + separator + "bin"
  // + separator + "java";
  // final ProcessBuilder processBuilder = new ProcessBuilder(path, "-cp",
  // classpath, this.mainClass.getName());
  // this.process = processBuilder.start();
  // this.process.waitFor();
  //
  // }

}
