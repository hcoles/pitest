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

import static org.pitest.util.Unchecked.translateCheckedException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author henry
 * 
 */
public class IsolatingClassLoader extends URLClassLoader {

  public IsolatingClassLoader() {
    super(getURLArray(), null);
  }

  private static URL[] getURLArray() {

    final String[] elements = ClassPath.getClassPathElements();

    final List<URL> us = new ArrayList<URL>();
    for (final String each : elements) {
      try {
        us.add(new File(each).toURL());
      } catch (final MalformedURLException e) {
        throw translateCheckedException(e);
      }

    }
    return us.toArray(new URL[] {});

  }

}
