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
package org.pitest.mutationtest.engine;

import java.util.Arrays;

import org.pitest.mutationtest.MutationDetails;

public final class Mutant {

  private final MutationDetails details;
  private final byte[]          bytes;

  public Mutant(final MutationDetails details, final byte[] bytes) {
    this.details = details;
    this.bytes = bytes;
  }

  public MutationDetails getDetails() {
    return this.details;
  }

  public byte[] getBytes() {
    return this.bytes;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(this.bytes);
    result = prime * result
        + ((this.details == null) ? 0 : this.details.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Mutant other = (Mutant) obj;
    if (!Arrays.equals(this.bytes, other.bytes)) {
      return false;
    }
    if (this.details == null) {
      if (other.details != null) {
        return false;
      }
    } else if (!this.details.equals(other.details)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "Mutant [details=" + this.details + "]";
  }

}
