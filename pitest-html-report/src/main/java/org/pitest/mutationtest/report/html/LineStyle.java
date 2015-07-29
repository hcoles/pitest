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

import org.pitest.mutationtest.DetectionStatus;

public class LineStyle {

  private final Line line;

  public LineStyle(final Line line) {
    this.line = line;
  }

  public String getLineCoverage() {
    switch (this.line.getLineCovered()) {
    case Covered:
      return "covered";
    case NotCovered:
      return "uncovered";
    default:
      return "na";
    }
  }

  public String getCode() {
    switch (this.line.getLineCovered()) {
    case Covered:
      return "covered";
    case NotCovered:
      return "uncovered";
    default:
      return "";
    }
  }

  public String getMutation() {
    if (this.line.detectionStatus().hasNone()) {
      return "";
    }

    final DetectionStatus status = this.line.detectionStatus().value();
    if (!status.isDetected()) {
      return "survived";
    }

    if (ConfidenceMap.hasHighConfidence(status)) {
      return "killed";
    } else {
      return "uncertain";
    }

  }

  public String getText() {
    return getLineCoverage();
  }

}
