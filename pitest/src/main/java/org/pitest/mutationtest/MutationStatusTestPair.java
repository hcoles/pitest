/*
 * Copyright 2011 Henry Coles
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
package org.pitest.mutationtest;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class MutationStatusTestPair implements Serializable {

  private static final long serialVersionUID = 1L;

  private int             numberOfTestsRun;
  private DetectionStatus status;
  private final List<String>    killingTests;
  private final List<String>    succeedingTests;
  private final List<String>    timeOutTests;
  private final List<String>    runErrorTests;
  private final List<String>    memoryErrorTests;

  public static MutationStatusTestPair notAnalysed(int testsRun, DetectionStatus status) {
    return new MutationStatusTestPair(testsRun, status, Collections.emptyList(), Collections.emptyList(),
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
  }

  public MutationStatusTestPair(final int numberOfTestsRun,
      final DetectionStatus status, final String killingTest) {
    this(numberOfTestsRun, status, killingTestToList(killingTest),
      Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
  }

  public MutationStatusTestPair(final int numberOfTestsRun,
                                final DetectionStatus status, final List<String> killingTests,
                                final List<String> succeedingTests) {
    this(numberOfTestsRun, status, killingTests, succeedingTests, Collections.emptyList(),
            Collections.emptyList(), Collections.emptyList());
  }

  public MutationStatusTestPair(final int numberOfTestsRun,
      final DetectionStatus status, final List<String> killingTests,
      final List<String> succeedingTests, final List<String> timeOutTests,
      final List<String> runErrorTests, final List<String> memoryErrorTests) {
    this.status = status;
    this.killingTests = new LinkedList<>(killingTests);
    this.succeedingTests = new LinkedList<>(succeedingTests);
    this.numberOfTestsRun = numberOfTestsRun;
    this. timeOutTests = new LinkedList<>(timeOutTests);
    this.runErrorTests = new LinkedList<>(runErrorTests);
    this.memoryErrorTests = new LinkedList<>(memoryErrorTests);
  }
  
  private static List<String> killingTestToList(String killingTest) {
    if (killingTest == null) {
      return Collections.emptyList();
    }
    
    return Collections.singletonList(killingTest);
  }

  public DetectionStatus getStatus() {
    return this.status;
  }

  /**
   * Get the killing test.
   * If the full mutation matrix is enabled, the first test will be returned.
   */
  public Optional<String> getKillingTest() {
    if (this.killingTests.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(this.killingTests.get(0));
  }

  /** Get all killing tests.
   *  If the full mutation matrix is not enabled, this will only be the first killing test. 
   */
  public List<String> getKillingTests() {
    return killingTests;
  }

  /** Get all succeeding tests.
   *  If the full mutation matrix is not enabled, this list will be empty. 
   */
  public List<String> getSucceedingTests() {
    return succeedingTests;
  }

  public int getNumberOfTestsRun() {
    return this.numberOfTestsRun;
  }

  @Override
  public String toString() {
    if (this.killingTests.isEmpty()) {
      return this.status.name();
    } else {
      return this.status.name() + " by " + this.killingTests;
    }

  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result)
        + ((this.killingTests == null) ? 0 : this.killingTests.hashCode());
    result = (prime * result)
        + ((this.succeedingTests == null) ? 0 : this.succeedingTests.hashCode());
    result = (prime * result)
            + ((this.memoryErrorTests == null) ? 0 : this.memoryErrorTests.hashCode());
    result = (prime * result)
            + ((this.runErrorTests == null) ? 0 : this.runErrorTests.hashCode());
    result = (prime * result)
            + ((this.timeOutTests == null) ? 0 : this.timeOutTests.hashCode());
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
    final MutationStatusTestPair other = (MutationStatusTestPair) obj;
    if (!Objects.equals( this.killingTests,other.killingTests)) {
      return false;
    }
    if (!Objects.equals(this.succeedingTests, other.succeedingTests)) {
      return false;
    }
    if (!Objects.equals(this.memoryErrorTests, other.memoryErrorTests)) {
      return false;
    }
    if (!Objects.equals(this.runErrorTests, other.runErrorTests)) {
      return false;
    }
    if (!Objects.equals(this.timeOutTests, other.timeOutTests)) {
      return false;
    }
    return true;
  }

  public void accumulate(MutationStatusTestPair status, String testName) {
    this.numberOfTestsRun += status.numberOfTestsRun;

    if (status.status.equals(DetectionStatus.KILLED)) {
      if (!this.killingTests.contains(testName)) {
        this.killingTests.add(testName);
      }
      this.succeedingTests.remove(testName);
    } else if (status.status.equals(DetectionStatus.SURVIVED) && !this.killingTests.contains(testName)
            && !this.succeedingTests.contains(testName)) {
      this.succeedingTests.add(testName);
    } else if (status.status.equals(DetectionStatus.MEMORY_ERROR)) {
      if (!this.memoryErrorTests.contains(testName)) {
        this.memoryErrorTests.add(testName);
      }
      this.succeedingTests.remove(testName);
    } else if (status.status.equals(DetectionStatus.RUN_ERROR)) {
      if (!this.runErrorTests.contains(testName)) {
        this.runErrorTests.add(testName);
      }
      this.succeedingTests.remove(testName);
    } else if (status.status.equals(DetectionStatus.TIMED_OUT)) {
      if (!this.timeOutTests.contains(testName)) {
        this.timeOutTests.add(testName);
      }
      this.succeedingTests.remove(testName);
    }

    if (!this.killingTests.isEmpty()) {
      this.status = DetectionStatus.KILLED;
    } else if (!this.runErrorTests.isEmpty()) {
      this.status = DetectionStatus.RUN_ERROR;
    } else if (!this.memoryErrorTests.isEmpty()) {
      this.status = DetectionStatus.MEMORY_ERROR;
    } else if (!this.timeOutTests.isEmpty()) {
      this.status = DetectionStatus.TIMED_OUT;
    } else if (!this.succeedingTests.isEmpty()) {
      this.status = DetectionStatus.SURVIVED;
    } else {
      this.status = status.status;
    }
  }

  public List<String> getTimeOutTests() {
    return timeOutTests;
  }

  public List<String> getRunErrorTests() {
    return runErrorTests;
  }

  public List<String> getMemoryErrorTests() {
    return memoryErrorTests;
  }

  public void setErrorStatusAndName (DetectionStatus status, String testName) {
    this.status = status;
    if (this.status == DetectionStatus.RUN_ERROR) {
      this.runErrorTests.add(testName);
    } else if (this.status == DetectionStatus.MEMORY_ERROR) {
      this.memoryErrorTests.add(testName);
    } else if (this.status == DetectionStatus.TIMED_OUT) {
      this.timeOutTests.add(testName);
    }
  }
}
