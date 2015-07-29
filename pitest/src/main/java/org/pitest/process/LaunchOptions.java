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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LaunchOptions {

  private final JavaAgent             javaAgentFinder;
  private final List<String>          childJVMArgs;
  private final JavaExecutableLocator javaExecutable;
  private final Map<String, String>   environmentVariables;

  public LaunchOptions(JavaAgent javaAgentFinder) {
    this(javaAgentFinder, new DefaultJavaExecutableLocator(), Collections
        .<String> emptyList(), new HashMap<String, String>());
  }

  public LaunchOptions(JavaAgent javaAgentFinder,
      JavaExecutableLocator javaExecutable, List<String> childJVMArgs,
      Map<String, String> environmentVariables) {
    this.javaAgentFinder = javaAgentFinder;
    this.childJVMArgs = childJVMArgs;
    this.javaExecutable = javaExecutable;
    this.environmentVariables = environmentVariables;
  }

  public JavaAgent getJavaAgentFinder() {
    return this.javaAgentFinder;
  }

  public List<String> getChildJVMArgs() {
    return this.childJVMArgs;
  }

  public String getJavaExecutable() {
    return this.javaExecutable.javaExecutable();
  }

  public Map<String, String> getEnvironmentVariables() {
    return this.environmentVariables;
  }
}
