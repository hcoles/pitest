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

package org.pitest.coverage.calculator;

/**
 * @author ivanalx
 * @date 28.01.2009 10:59:10
 */
public class InvokeEntry {
  private final InvokeType type;
  private final int        classId;
  private final int        codeId;

  public InvokeEntry(final InvokeType type, final int classId, final int codeId) {
    this.type = type;
    this.classId = classId;
    this.codeId = codeId;
  }

  public InvokeType getType() {
    return this.type;
  }

  public int getClassId() {
    return this.classId;
  }

  public int getCodeId() {
    return this.codeId;
  }

  @Override
  public String toString() {
    return "InvokeEntry{" + "type=" + this.type + ", classId=" + this.classId
        + ", codeId=" + this.codeId + '}';
  }
}
