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
package org.pitest.mutationtest.instrument;

import java.io.File;
import java.util.regex.Pattern;

import org.pitest.functional.Option;
import org.pitest.util.JavaAgent;

public class JavaAgentJarFinder implements JavaAgent {

  private static final Pattern JAR_REGEX = Pattern
                                             .compile(".*pitest-[\\w-\\.]*.jar");
  private final String         location;

  public JavaAgentJarFinder() {
    String pitJar = findPathToJarFileFromClasspath();
    if (pitJar == null) {
      pitJar = "pitest-agent.jar";
    }
    this.location = pitJar;
  }

  private String findPathToJarFileFromClasspath() {
    final String[] classPath = System.getProperty("java.class.path").split(
        File.pathSeparator);

    for (final String root : classPath) {
      if (JAR_REGEX.matcher(root).matches()) {
        return root;
      }
    }

    return null;
  }

  public Option<String> getJarLocation() {
    // TODO Auto-generated method stub
    return Option.some(this.location);
  }

}
