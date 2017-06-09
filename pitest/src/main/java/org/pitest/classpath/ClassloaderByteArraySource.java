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
package org.pitest.classpath;

import java.io.IOException;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.functional.Option;
import org.pitest.util.IsolationUtils;
import org.pitest.util.Unchecked;

public class ClassloaderByteArraySource implements ClassByteArraySource {

  private final ClassPath cp;

  public ClassloaderByteArraySource(final ClassLoader loader) {
    this.cp = new ClassPath(new OtherClassLoaderClassPathRoot(loader));
  }
  
  public static ClassloaderByteArraySource fromContext() {
    return new ClassloaderByteArraySource(IsolationUtils.getContextClassLoader());
  }

  @Override
  public Option<byte[]> getBytes(final String classname) {
    try {
      return Option.some(this.cp.getClassData(classname));
    } catch (final IOException ex) {
      throw Unchecked.translateCheckedException(ex);
    }
  }

}
