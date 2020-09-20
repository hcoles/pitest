package org.pitest.mutationtest.report.html;

import static org.pitest.mutationtest.report.html.MutationScoreUtil.groupBySameMutationOnSameLines;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.report.html.ResultComparator.DetectionStatusComparator;

/**
 * Compute the {@link DetectionStatus} according to the list of mutations applied on the line.
 * This algorithm works with multi-projects. For example:
 * 
 * <strong>Tested class</strong>
 * <pre><code>
 * 1   class Foo {
 * 2     public int compare() {
 * 3       if (a > b) {
 * 4         return -1;
 * 5       }
 * 6       if (a < b) {
 * 7         return 1;
 * 8       }
 * 9       return 0;
 * 10    }
 * 11  }
 * </pre></code>
 * 
 * Module A contains Foo class.
 * Module B depends on Module A.
 * 
 * <strong>Test results 1</strong>
 * <table>
 * <caption>mutations</caption>
 * <thead><tr><th>Module</th><th>Test</th><th>Mutation results</th></tr></thead>
 * <tbody>
 * <tr><td>Module A</td><td>UnitTest.identical()</td><td><ul><li>line 9: return replaced by -1 -> SURVIVED</li></ul></td></tr>
 * <tr><td>Module A</td><td>UnitTest.identical()</td><td><ul><li>line 9: return replaced by 1 -> SURVIVED</li></ul></td></tr>
 * <tr><td>Module B</td><td>IntegrationTest.sort()</td><td><ul><li>line 9: return replaced by -1 -> KILLED</li></ul></td></tr>
 * <tr><td>Module B</td><td>IntegrationTest.sort()</td><td><ul><li>line 9: return replaced by 1 -> SURVIVED</li></ul></td></tr>
 * </tbody>
 * </table>
 * 
 * There are several mutations applied on the same line and one mutation has survived across all tests and one mutation has been killed.
 * So the result should be SURVIVED because for the same line, at least one mutation has survived.
 * 
 * <strong>Test results 2</strong>
 * <table>
 * <caption>mutations</caption>
 * <thead><tr><th>Module</th><th>Test</th><th>Mutation results</th></tr></thead>
 * <tbody>
 * <tr><td>Module A</td><td>UnitTest.identical()</td><td><ul><li>line 9: return replaced by -1 -> SURVIVED</li></ul></td></tr>
 * <tr><td>Module A</td><td>UnitTest.identical()</td><td><ul><li>line 9: return replaced by 1 -> KILLED</li></ul></td></tr>
 * <tr><td>Module B</td><td>IntegrationTest.sort()</td><td><ul><li>line 9: return replaced by -1 -> KILLED</li></ul></td></tr>
 * <tr><td>Module B</td><td>IntegrationTest.sort()</td><td><ul><li>line 9: return replaced by 1 -> SURVIVED</li></ul></td></tr>
 * </tbody>
 * </table>
 * 
 * In this case, all mutations are killed across several projects. So the result should be KILLED.
 * 
 * @author Aurélien Baudet
 *
 */
public class MergeStatusForSameMutationAndLine implements DetectionStatusCalculator {
  @Override
  public DetectionStatus calculate(List<MutationResult> mutationsForLine) {
    if (mutationsForLine.isEmpty()) {
      return null;
    }
    Map<MutationIdentifier, Collection<MutationResult>> grouped = groupBySameMutationOnSameLines(mutationsForLine);
    Map<MutationIdentifier, DetectionStatus> statusForSameMutation = new HashMap<>();
    // compute the status for each mutation
    for (Entry<MutationIdentifier, Collection<MutationResult>> resultsByMutation : grouped.entrySet()) {
      statusForSameMutation.put(resultsByMutation.getKey(), computeDetectionStatusForSameLineAndMutation(resultsByMutation.getValue()));
    }
    // for different mutations, take the worst mutation
    List<DetectionStatus> status = new ArrayList<>(statusForSameMutation.values());
    Collections.sort(status, new DetectionStatusComparator());
    if (status.isEmpty()) {
      return null;
    }
    return status.get(0);
  }

  private DetectionStatus computeDetectionStatusForSameLineAndMutation(Collection<MutationResult> results) {
    List<MutationResult> list = new ArrayList<>(results);
    Collections.sort(list, new ResultComparator());
    if (list.isEmpty()) {
      return null;
    }
    // use best status to handle several tests across projects
    return list.get(list.size() - 1).getStatus();
  }
}
