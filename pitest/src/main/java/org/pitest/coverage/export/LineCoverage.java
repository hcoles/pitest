package org.pitest.coverage.export;

import java.util.Collection;

import org.pitest.mutationtest.instrument.ClassLine;

public class LineCoverage {
  
  private final ClassLine classLine;
  private final Collection<String> tests;
  
  public LineCoverage( ClassLine classLine, Collection<String> tests) {
    this.classLine = classLine;
    this.tests = tests;
  }

  public ClassLine getClassLine() {
    return classLine;
  }

  public Collection<String> getTests() {
    return tests;
  }

}
