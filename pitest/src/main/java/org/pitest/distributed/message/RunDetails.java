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

package org.pitest.distributed.message;

import java.io.Serializable;
import java.net.InetSocketAddress;

import com.thoughtworks.xstream.core.util.Base64Encoder;

public final class RunDetails implements Serializable {

  private static final long       serialVersionUID = 1L;

  private final InetSocketAddress master;
  private final long              dtime;

  public RunDetails(final InetSocketAddress master, final long date) {
    this.master = master;
    this.dtime = date;
  }

  public InetSocketAddress getMasterAddress() {
    return this.master;
  }

  public String getIdentifier() {
    final String id = this.master.getAddress().getHostAddress() + this.dtime;
    return new Base64Encoder().encode(id.getBytes()).replace("=", "");
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (this.dtime ^ (this.dtime >>> 32));
    result = prime * result
        + ((this.master == null) ? 0 : this.master.hashCode());
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
    final RunDetails other = (RunDetails) obj;
    if (this.dtime != other.dtime) {
      return false;
    }
    if (this.master == null) {
      if (other.master != null) {
        return false;
      }
    } else if (!this.master.equals(other.master)) {
      return false;
    }
    return true;
  }

  public InetSocketAddress getInetSocketAddress() {
    return this.master;
  }

}
