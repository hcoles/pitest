/*
 * Copyright 2015 Jason Fehr
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
package org.pitest.maven.report.generator;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pitest.maven.report.ReportSourceLocator;
import org.pitest.util.PitError;

@RunWith(MockitoJUnitRunner.class)
public class ReportGenerationManagerTest {

  private ReportGenerationContext        generationContext;
  private List<ReportGenerationStrategy> reportGenerationStrategyList;

  @Mock
  private ReportSourceLocator            reportLocator;
  @Mock
  private XMLReportGenerator             xmlGenerator;
  @Mock
  private HTMLReportGenerator            htmlGenerator;
  @Mock
  private Log                            log;
  @Mock
  private File                           reportsDataDirectory;
  @Mock
  private File                           siteDirectory;
  @Mock
  private File                           locatedReportsDataDirectory;

  private ReportGenerationManager        fixture;

  @Before
  public void setUp() {
    this.reportGenerationStrategyList = new LinkedList<>();
    this.reportGenerationStrategyList.add(this.xmlGenerator);
    this.reportGenerationStrategyList.add(this.htmlGenerator);

    this.fixture = new ReportGenerationManager(this.reportLocator,
        this.reportGenerationStrategyList);

    this.generationContext = new ReportGenerationContext(Locale.ENGLISH, null,
        this.reportsDataDirectory, this.siteDirectory, this.log, Arrays.asList(
            "XML", "HTML"));

    when(this.reportLocator.locate(this.reportsDataDirectory, this.log))
        .thenReturn(this.locatedReportsDataDirectory);
    when(this.xmlGenerator.getGeneratorDataFormat()).thenReturn("XML");
    when(this.htmlGenerator.getGeneratorDataFormat()).thenReturn("HTML");
  }

  @Test
  public void testFirstGeneratorSuccess() {
    when(this.xmlGenerator.generate(this.generationContext)).thenReturn(
        ReportGenerationResultEnum.SUCCESS);

    this.fixture.generateSiteReport(this.generationContext);

    verify(this.xmlGenerator).generate(this.generationContext);
    verifyZeroInteractions(this.htmlGenerator);
    this.assertLocatedReportsDirectory();
  }

  @Test
  public void testFirstGeneratorNotRunSecondGeneratorSuccess() {
    when(this.xmlGenerator.generate(this.generationContext)).thenReturn(
        ReportGenerationResultEnum.NOT_EXECUTED);
    when(this.htmlGenerator.generate(this.generationContext)).thenReturn(
        ReportGenerationResultEnum.SUCCESS);

    this.fixture.generateSiteReport(this.generationContext);

    verify(this.xmlGenerator).generate(this.generationContext);
    verify(this.htmlGenerator).generate(this.generationContext);
    this.assertLocatedReportsDirectory();
  }

  @Test(expected = PitError.class)
  public void testFirstGeneratorNotRunSecondGeneratorNotRun() {
    when(this.xmlGenerator.generate(this.generationContext)).thenReturn(
        ReportGenerationResultEnum.NOT_EXECUTED);
    when(this.htmlGenerator.generate(this.generationContext)).thenReturn(
        ReportGenerationResultEnum.NOT_EXECUTED);

    this.fixture.generateSiteReport(this.generationContext);
    this.assertLocatedReportsDirectory();
  }

  @Test
  public void testFirstGeneratorFailSecondGeneratorSuccess() {
    when(this.xmlGenerator.generate(this.generationContext)).thenReturn(
        ReportGenerationResultEnum.FAILURE);
    when(this.htmlGenerator.generate(this.generationContext)).thenReturn(
        ReportGenerationResultEnum.SUCCESS);

    this.fixture.generateSiteReport(this.generationContext);

    verify(this.xmlGenerator).generate(this.generationContext);
    verify(this.htmlGenerator).generate(this.generationContext);
    this.assertLocatedReportsDirectory();
  }

  @Test(expected = PitError.class)
  public void testFirstGeneratorFailSecondGeneratorFail() {
    when(this.xmlGenerator.generate(this.generationContext)).thenReturn(
        ReportGenerationResultEnum.FAILURE);
    when(this.htmlGenerator.generate(this.generationContext)).thenReturn(
        ReportGenerationResultEnum.FAILURE);

    this.fixture.generateSiteReport(this.generationContext);
    this.assertLocatedReportsDirectory();
  }

  @Test(expected = PitError.class)
  public void testNoGeneratorsFound() {
    this.generationContext.setSourceDataFormats(Arrays.asList("foo"));
    this.fixture.generateSiteReport(this.generationContext);
  }

  private void assertLocatedReportsDirectory() {
    assertThat(this.generationContext.getReportsDataDirectory(),
        sameInstance(this.locatedReportsDataDirectory));
  }

}
