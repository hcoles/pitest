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
package org.pitest.mutationtest.report.html;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassName;
import org.pitest.coverage.CoverageDatabase;
import java.util.Optional;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.SourceLocator;
import org.pitest.mutationtest.report.MutationTestResultMother;
import org.pitest.util.ResultOutputStrategy;

public class MutationHtmlReportListenerTest {

  private MutationHtmlReportListener testee;

  @Mock
  private CoverageDatabase           coverageDb;

  @Mock
  private ResultOutputStrategy       outputStrategy;

  @Mock
  private SourceLocator              sourceLocator;

  @Mock
  private Writer                     writer;

  @Mock
  private ClassInfo                  classInfo;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    when(this.outputStrategy.createWriterForFile(any(String.class)))
        .thenReturn(this.writer);
    when(this.classInfo.getName()).thenReturn(ClassName.fromString("foo"));
    when(this.coverageDb.getClassInfo(any(Collection.class))).thenReturn(
        Collections.singleton(this.classInfo));

    this.testee = new MutationHtmlReportListener(this.coverageDb,
        this.outputStrategy, Collections.<String>emptyList(), this.sourceLocator);
  }

  @Test
  public void shouldCreateAnIndexFile() {
    this.testee.onRunEnd();
    verify(this.outputStrategy).createWriterForFile("index.html");
  }

  @Test
  public void shouldCreateACssFile() {
    this.testee.onRunEnd();
    verify(this.outputStrategy).createWriterForFile("style.css");
  }

  @Test
  public void shouldTryToLocateSourceFilesFromMutatedClasses() {
    final String fileName = "foo.java";
    final MutationResult mr = new MutationResult(
        MutationTestResultMother.createDetails(fileName),
        new MutationStatusTestPair(1, DetectionStatus.KILLED, "testName"));
    when(this.sourceLocator.locate(any(Collection.class), any(String.class)))
        .thenReturn(Optional.<Reader> empty());
    this.testee.handleMutationResult(MutationTestResultMother
        .createClassResults(mr));
    verify(this.sourceLocator).locate(any(Collection.class), eq(fileName));
  }

}
