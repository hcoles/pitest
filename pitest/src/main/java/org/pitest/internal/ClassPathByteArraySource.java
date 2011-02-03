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
package org.pitest.internal;

import java.io.IOException;

import org.pitest.functional.Option;

public class ClassPathByteArraySource implements ClassByteArraySource {

  private final ClassPath classPath;

  public ClassPathByteArraySource() {
    this(new ClassPath());
  }

  public ClassPathByteArraySource(final ClassPath classPath) {
    this.classPath = classPath;
  }

  public Option<byte[]> apply(final String classname) {
    try {
      return Option.some(this.classPath.getClassData(classname));
    } catch (final IOException e) {
      return Option.none();
    }
  }
}
