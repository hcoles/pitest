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

import java.io.IOException;
import java.io.Writer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.TestResult;
import org.pitest.mutationtest.execute.MutationStatusTestPair;
import org.pitest.mutationtest.results.DetectionStatus;
import org.pitest.mutationtest.results.MutationResult;

public class CSVReportListenerTest {

  private static final String NEW_LINE = System.getProperty("line.separator");

  private CSVReportListener   testee;

  @Mock
  private Writer              out;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    this.testee = new CSVReportListener(this.out);
  }

  @Test
  public void shouldOutputKillingTestWhenOneFound() throws IOException {
    final MutationResult mr = new MutationResult(
        MutationTestResultMother.createDetails(), new MutationStatusTestPair(
            DetectionStatus.KILLED, "foo"));
    final TestResult tr = createResult(mr);
    this.testee.onTestSuccess(tr);
    final String expected = "file,class,method,42,KILLED,foo" + NEW_LINE;
    verify(this.out).write(expected);
  }

  @Test
  public void shouldOutputNoneWhenNoKillingTestFound() throws IOException {
    final MutationResult mr = new MutationResult(
        MutationTestResultMother.createDetails(), new MutationStatusTestPair(
            DetectionStatus.SURVIVED));
    final TestResult tr = createResult(mr);
    this.testee.onTestSuccess(tr);
    final String expected = "file,class,method,42,SURVIVED,none" + NEW_LINE;
    ;
    verify(this.out).write(expected);
  }

  private TestResult createResult(final MutationResult... mrs) {
    return MutationTestResultMother.createResult(MutationTestResultMother
        .createMetaData(mrs));
  }

}
