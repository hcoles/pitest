package org.pitest.mutationtest.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.pitest.classinfo.ClassName;
import org.pitest.coverage.BlockLocation;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.coverage.TestInfo;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.tdg.Tdgimpl;
/**
 * Assigns tests based on line coverage and order them by execution speed with a
 * weighting towards tests whose names imply they are intended to test the
 * mutated class
 *
 * @author henry
 *
 */
public class TdgTestPrioritiser implements TestPrioritiser {

  private static final int       TIME_WEIGHTING_FOR_DIRECT_UNIT_TESTS = 1000;

  private final Tdgimpl tdg;

  public TdgTestPrioritiser(Tdgimpl tdg) {
    this.tdg = tdg;
  }

  @Override
  public List<TestInfo> assignTests(MutationDetails mutation) {
    return prioritizeTests(mutation.getClassName(), pickTests(mutation));
  }

  private Collection<TestInfo> pickTests(MutationDetails mutation) {
    return tdg.getTests(mutation.getClassName());
  }

  private List<TestInfo> prioritizeTests(ClassName clazz,
      Collection<TestInfo> testsForMutant) {
    final List<TestInfo> sortedTis = new ArrayList<>(testsForMutant);
    sortedTis.sort(new MyTestInfoPriorisationComparator(clazz, TIME_WEIGHTING_FOR_DIRECT_UNIT_TESTS));
    return sortedTis;
  }

}
