/*
 * Based on http://code.google.com/p/javacoveragent/ by
 * "alex.mq0" and "dmitry.kandalov"
 * 
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

package org.pitest.coverage;

/**
 * @author ivanalx
 * @date 28.01.2009 10:59:10
 */
public class InvokeEntry {

  private final int classId;
  private final int lineNumber;

  public InvokeEntry(final int classId, final int codeId) {
    this.classId = classId;
    this.lineNumber = codeId;
  }

  public int getClassId() {
    return this.classId;
  }

  public int getLineNumber() {
    return this.lineNumber;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + this.classId;
    result = prime * result + this.lineNumber;
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
    final InvokeEntry other = (InvokeEntry) obj;
    if (this.classId != other.classId) {
      return false;
    }
    if (this.lineNumber != other.lineNumber) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "InvokeEntry [classId=" + this.classId + ", lineNumber="
        + this.lineNumber + "]";
  }

}
