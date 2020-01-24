package org.pitest.mutationtest.report.html;

import java.util.List;

import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationResult;

public interface DetectionStatusCalculator {
  DetectionStatus calculate(List<MutationResult> mutationsForLine);
}
