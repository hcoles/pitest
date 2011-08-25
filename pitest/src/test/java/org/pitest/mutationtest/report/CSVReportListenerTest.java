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
package org.pitest.mutationtest.report;

import static org.mockito.Mockito.verify;

import java.io.PrintWriter;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.Description;
import org.pitest.DescriptionMother;
import org.pitest.ExtendedTestResult;
import org.pitest.TestResult;
import org.pitest.mutationtest.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.execute.MutationStatusTestPair;
import org.pitest.mutationtest.instrument.MutationMetaData;
import org.pitest.mutationtest.results.DetectionStatus;
import org.pitest.mutationtest.results.MutationResult;

public class CSVReportListenerTest {

  private CSVReportListener testee;

  @Mock
  private PrintWriter       out;
  private Description       description;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    this.description = DescriptionMother.createEmptyDescription("foo");
    this.testee = new CSVReportListener(this.out);
  }

  @Test
  public void shouldOutputKillingTestWhenOneFound() {
    final MutationResult mr = new MutationResult(createDetails(),
        new MutationStatusTestPair(DetectionStatus.KILLED, "foo"));
    final TestResult tr = createResult(mr);
    this.testee.onTestSuccess(tr);
    final String expected = "file,class,method,42,KILLED,foo";
    verify(this.out).println(expected);
  }

  @Test
  public void shouldOutputNoneWhenNoKillingTestFound() {
    final MutationResult mr = new MutationResult(createDetails(),
        new MutationStatusTestPair(DetectionStatus.SURVIVED));
    final TestResult tr = createResult(mr);
    this.testee.onTestSuccess(tr);
    final String expected = "file,class,method,42,SURVIVED,none";
    verify(this.out).println(expected);
  }

  private MutationDetails createDetails() {
    return new MutationDetails(new MutationIdentifier("class", 1, "mutator"),
        "file", "desc", "method", 42);
  }

  private TestResult createResult(final MutationResult... mrs) {
    return createResult(createMetaData(mrs));
  }

  private MutationMetaData createMetaData(final MutationResult... mrs) {
    return new MutationMetaData(null, Arrays.asList(mrs));
  }

  private TestResult createResult(final MutationMetaData md) {
    return new ExtendedTestResult(this.description, null, md);
  }
}
