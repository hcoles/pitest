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
package org.pitest.mutationtest.statistics;

import org.pitest.mutationtest.DetectionStatus;

public class StatusCount {

  private final DetectionStatus status;
  private long                  count;

  StatusCount(final DetectionStatus status, final long count) {
    this.status = status;
    this.count = count;
  }

  void increment() {
    this.count++;
  }

  @Override
  public String toString() {
    return "" + this.status + " " + this.count;
  }

  public long getCount() {
    return this.count;
  }

  public DetectionStatus getStatus() {
    return this.status;
  }
}