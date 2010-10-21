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
package org.pitest.mutationtest.hotswap;

import java.util.List;

import org.pitest.extension.TestUnit;

public class RunDetails {
  private int            startMutation;
  private int            endMutation;
  private String         className;
  // private long normalExecutionTime;
  private List<TestUnit> tests;

  public int getStartMutation() {
    return this.startMutation;
  }

  public void setStartMutation(final int startMutation) {
    this.startMutation = startMutation;
  }

  public int getEndMutation() {
    return this.endMutation;
  }

  public void setEndMutation(final int endMutation) {
    this.endMutation = endMutation;
  }

  public String getClassName() {
    return this.className;
  }

  public void setClassName(final String className) {
    this.className = className;
  }

  // public long getNormalExecutionTime() {
  // return this.normalExecutionTime;
  // }
  //
  // public void setNormalExecutionTime(final long normalExecutionTime) {
  // this.normalExecutionTime = normalExecutionTime;
  // }

  public List<TestUnit> getTests() {
    return this.tests;
  }

  public void setTests(final List<TestUnit> tests) {
    this.tests = tests;
  }

  @Override
  public String toString() {
    return "RunDetails [className=" + this.className + ", endMutation="
        + this.endMutation + ", startMutation=" + this.startMutation
        + ", tests=" + this.tests + "]";
  }

}
