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
 * @date 29.01.2009 14:13:31
 */
public class MethodNameDescription {
  private final String name;
  private final String desc;

  public MethodNameDescription(final String name, final String desc) {
    this.name = name;
    this.desc = desc;
  }

  public String getName() {
    return this.name;
  }

  public String getDesc() {
    return this.desc;
  }

  public String asJumbleDescription() {
    return this.name + this.desc;
  }

  @Override
  public String toString() {
    return "MethodNameStore{" + "name='" + this.name + '\'' + ", desc='"
        + this.desc + '\'' + '}';
  }
}
