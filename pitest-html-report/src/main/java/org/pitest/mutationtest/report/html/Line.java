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
package org.pitest.mutationtest.report.html;

import java.util.Collections;
import java.util.List;

import java.util.Optional;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationResult;

public class Line {
  private final long                 number;
  private final String               text;
  private final LineStatus           lineCovered;
  private final List<MutationResult> mutations;

  public Line(final long number, final String text,
      final LineStatus lineCovered, final List<MutationResult> mutations) {
    this.number = number;
    this.text = text;
    this.lineCovered = lineCovered;
    this.mutations = mutations;
    Collections.sort(mutations, new ResultComparator());
  }

  public long getNumber() {
    return this.number;
  }

  public String getText() {
    return this.text;
  }

  public LineStatus getLineCovered() {
    return this.lineCovered;
  }

  public List<MutationResult> getMutations() {
    return this.mutations;
  }

  public Optional<DetectionStatus> detectionStatus() {
    if (this.mutations.isEmpty()) {
      return Optional.empty();
    }
    return Optional.ofNullable(this.mutations.get(0).getStatus());
  }

  public int getNumberOfMutations() {
    return this.mutations.size();
  }

  public String getNumberOfMutationsForDisplay() {
    if (getNumberOfMutations() > 0) {
      return "" + getNumberOfMutations();
    } else {
      return "";
    }
  }

  public LineStyle getStyles() {
    return new LineStyle(this);
  }

}
