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
package org.pitest.maven.report;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Locale;

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.plugin.logging.Log;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pitest.maven.report.generator.ReportGenerationContext;
import org.pitest.maven.report.generator.ReportGenerationManager;
import org.pitest.util.PitError;

@RunWith(MockitoJUnitRunner.class)
public class PitReportMojoTest {

  @Captor
  private ArgumentCaptor<ReportGenerationContext> contextCaptor;

  @Mock
  private Log                                     log;
  @Mock
  private File                                    reportsDirectory;
  @Mock
  private ReportGenerationManager                 reportGenerationManager;
  @Mock
  private Sink                                    sink;

  @InjectMocks
  private PitReportMojo                           fixture;

  @Test(expected = PitError.class)
  public void testNonExistantReportsDirectory() throws Exception {
    this.setupMocks(false, true, true);
    this.fixture.executeReport(Locale.ENGLISH);
  }

  @Test(expected = PitError.class)
  public void testNonReadableReportsDirectory() throws Exception {
    this.setupMocks(true, false, true);
    this.fixture.executeReport(Locale.ENGLISH);
  }

  @Test(expected = PitError.class)
  public void testFileReportsDirectory() throws Exception {
    this.setupMocks(true, true, false);
    this.fixture.executeReport(Locale.ENGLISH);
  }

  @Test
  public void testGenerateReport() throws Exception {
    ReportGenerationContext actualContext;

    this.reflectionSetSiteReportDir("pit-reports");

    setupMocks(true, true, true);
    when(this.reportsDirectory.getAbsolutePath()).thenReturn("abspath");

    this.fixture.executeReport(Locale.ENGLISH);

    verify(this.reportGenerationManager).generateSiteReport(
        this.contextCaptor.capture());
    actualContext = this.contextCaptor.getValue();

    assertThat(actualContext.getLocale(), sameInstance(Locale.ENGLISH));
    assertThat(actualContext.getLogger(), sameInstance(this.log));
    assertThat(actualContext.getReportsDataDirectory(),
        sameInstance(this.reportsDirectory));
    assertThat(actualContext.getSink(), sameInstance(this.sink));
    assertThat(actualContext.getSiteDirectory().getPath(), is("abspath"
        + File.separator + "pit-reports"));
  }

  private void setupMocks(boolean reportsDirectoryExists,
      boolean reportsDirectoryReadable, boolean reportsDirectoryIsDirectory) {
    when(this.reportsDirectory.exists()).thenReturn(reportsDirectoryExists);
    when(this.reportsDirectory.canRead()).thenReturn(reportsDirectoryReadable);
    when(this.reportsDirectory.isDirectory()).thenReturn(
        reportsDirectoryIsDirectory);
  }

  private void reflectionSetSiteReportDir(String value) throws Exception {
    Field f = this.fixture.getClass().getDeclaredField("siteReportDirectory");
    f.setAccessible(true);
    f.set(this.fixture, value);
  }

}
