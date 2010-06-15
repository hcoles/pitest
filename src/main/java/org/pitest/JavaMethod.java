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

package org.pitest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;

public final class JavaMethod implements Serializable {

  private static final long serialVersionUID = 1L;

  private transient Method  method;

  public JavaMethod(final Method method) {
    this.method = method;
  }

  public Method method() {
    return this.method;
  }

  private void readObject(final ObjectInputStream aInputStream)
      throws ClassNotFoundException, IOException {

    aInputStream.defaultReadObject();
    final Class<?> clazz = (Class<?>) aInputStream.readObject();
    final String name = (String) aInputStream.readObject();
    final Class<?>[] params = (Class<?>[]) aInputStream.readObject();

    try {
      this.method = clazz.getDeclaredMethod(name, params);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }

  }

  private void writeObject(final ObjectOutputStream aOutputStream)
      throws IOException {

    aOutputStream.defaultWriteObject();
    aOutputStream.writeObject(this.method.getDeclaringClass());
    aOutputStream.writeObject(this.method.getName());
    aOutputStream.writeObject(this.method.getParameterTypes());
  }

}
