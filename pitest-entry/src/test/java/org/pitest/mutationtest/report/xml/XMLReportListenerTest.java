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
package org.pitest.mutationtest.report.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.report.MutationTestResultMother;

public class XMLReportListenerTest {

  private XMLReportListener testee;

  private Writer            out;

  @Before
  public void setup() {
    this.out = new StringWriter();
    this.testee = new XMLReportListener(this.out, false);
  }

  @Test
  public void shouldCreateAValidXmlDocumentWhenNoResults() throws IOException {
    this.testee.runStart();
    this.testee.runEnd();
    final String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<mutations>\n</mutations>\n";
    assertEquals(expected, this.out.toString());
  }

  @Test
  public void shouldOutputKillingTestWhenOneFound() throws IOException {
    final MutationResult mr = createdKilledMutationWithKillingTestOf("foo");
    this.testee
        .handleMutationResult(MutationTestResultMother.createClassResults(mr));
    final String expected = "<mutation detected='true' status='KILLED' numberOfTestsRun='1'><sourceFile>file</sourceFile><mutatedClass>clazz</mutatedClass><mutatedMethod>method</mutatedMethod><methodDescription>()I</methodDescription><lineNumber>42</lineNumber><mutator>mutator</mutator><index>1</index><block>0</block><killingTest>foo</killingTest><description>desc</description></mutation>\n";
    assertEquals(expected, this.out.toString());
  }

  @Test
  public void shouldOutputFullMutationMatrixWhenEnabled() throws IOException {
    this.testee = new XMLReportListener(this.out, true);
    final MutationResult mr = new MutationResult(
            MutationTestResultMother.createDetails(),
            new MutationStatusTestPair(3, DetectionStatus.KILLED, Arrays.asList("foo", "foo2"), Arrays.asList("bar")));
    this.testee
        .handleMutationResult(MutationTestResultMother.createClassResults(mr));
    final String expected = "<mutation detected='true' status='KILLED' numberOfTestsRun='3'><sourceFile>file</sourceFile><mutatedClass>clazz</mutatedClass><mutatedMethod>method</mutatedMethod><methodDescription>()I</methodDescription><lineNumber>42</lineNumber><mutator>mutator</mutator><index>1</index><block>0</block><killingTests>foo|foo2</killingTests><succeedingTests>bar</succeedingTests><description>desc</description></mutation>\n";
    assertEquals(expected, this.out.toString());
  }

  @Test
  public void shouldEscapeGTAndLTSymbols() {
    final MutationResult mr = createdKilledMutationWithKillingTestOf("<foo>");
    this.testee
        .handleMutationResult(MutationTestResultMother.createClassResults(mr));
    assertTrue(this.out.toString().contains("&#60;foo&#62;"));
  }

  @Test
  public void shouldEscapeNullBytes() {
    final MutationResult mr = createdKilledMutationWithKillingTestOf("\0 Null-Byte");
    this.testee
            .handleMutationResult(MutationTestResultMother.createClassResults(mr));
    assertTrue(this.out.toString().contains("\\0 Null-Byte"));
  }

  private MutationResult createdKilledMutationWithKillingTestOf(
      final String killingTest) {
    final MutationResult mr = new MutationResult(
        MutationTestResultMother.createDetails(),
        new MutationStatusTestPair(1, DetectionStatus.KILLED, killingTest));
    return mr;
  }

  @Test
  public void shouldOutputNoneWhenNoKillingTestFound() throws IOException {
    final MutationResult mr = createSurvivingMutant();
    this.testee
        .handleMutationResult(MutationTestResultMother.createClassResults(mr));
    final String expected = "<mutation detected='false' status='SURVIVED' numberOfTestsRun='1'><sourceFile>file</sourceFile><mutatedClass>clazz</mutatedClass><mutatedMethod>method</mutatedMethod><methodDescription>()I</methodDescription><lineNumber>42</lineNumber><mutator>mutator</mutator><index>1</index><block>0</block><killingTest/><description>desc</description></mutation>\n";
    assertEquals(expected, this.out.toString());
  }

  private MutationResult createSurvivingMutant() {
    final MutationResult mr = new MutationResult(
        MutationTestResultMother.createDetails(),
        new MutationStatusTestPair(1, DetectionStatus.SURVIVED));
    return mr;
  }

}
