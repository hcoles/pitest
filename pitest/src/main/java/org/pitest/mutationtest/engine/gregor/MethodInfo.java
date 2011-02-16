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
package org.pitest.mutationtest.engine.gregor;

import java.util.Arrays;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class MethodInfo {

  private final String   owner;
  private final int      access;
  private final String   name;
  private final String   desc;
  private final String   signature;
  private final String[] exceptions;

  public MethodInfo(final String owner, final int access, final String name,
      final String desc, final String signature, final String[] exceptions) {
    this.owner = owner;
    this.access = access;
    this.name = name;
    this.desc = desc;
    this.signature = signature;
    this.exceptions = exceptions;
  }

  public String getOwner() {
    return this.owner;
  }

  public int getAccess() {
    return this.access;
  }

  public String getName() {
    return this.name;
  }

  public String getDesc() {
    return this.desc;
  }

  public String getSignature() {
    return this.signature;
  }

  public String[] getExceptions() {
    return this.exceptions;
  }

  @Override
  public String toString() {
    return "MethodInfo [access=" + this.access + ", desc=" + this.desc
        + ", exceptions=" + Arrays.toString(this.exceptions) + ", name="
        + this.name + ", signature=" + this.signature + "]";
  }

  public boolean isSynthetic() {
    return ((this.access & Opcodes.ACC_SYNTHETIC) != 0);
  }

  public boolean isConstructor() {
    return isConstructor(this.name);
  }

  public static boolean isConstructor(final String name) {
    return "<init>".equals(name);
  }

  public Type getReturnType() {
    return Type.getReturnType(this.desc);
  }

  public Boolean isStaticInitializer() {
    return "<clinit>".equals(this.name);
  }

}
