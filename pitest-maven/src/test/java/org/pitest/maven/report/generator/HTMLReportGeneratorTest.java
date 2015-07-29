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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.util.Arrays;

import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class HTMLReportGeneratorTest {

  private static final String HTML_SOURCE_DATA_FORMAT = "HTML";

  @Rule
  public TemporaryFolder      tempFolder              = new TemporaryFolder();

  private Log                 mockLogger;
  private HTMLReportGenerator fixture;

  @Before
  public void setUp() {
    this.mockLogger = mock(Log.class);
    this.fixture = new HTMLReportGenerator();
  }

  @Test
  public void testName() {
    assertThat(this.fixture.getGeneratorName(), is("HTMLReportGenerator"));
  }

  @Test
  public void testCopySuccess() throws Exception {
    File sourceFolder = this.tempFolder.newFolder("reportsFolder");
    File destFolder = this.tempFolder.newFolder("siteFolder");

    new File(sourceFolder, "file0.txt").createNewFile();
    new File(sourceFolder, "file1.txt").createNewFile();

    assertThat(this.fixture.generate(new ReportGenerationContext(null, null,
        sourceFolder, destFolder, this.mockLogger, Arrays
            .asList(HTML_SOURCE_DATA_FORMAT))),
        sameInstance(ReportGenerationResultEnum.SUCCESS));
    assertThat(sourceFolder.listFiles().length, is(2));
    assertThat(destFolder.listFiles().length, is(2));
  }

  @Test
  public void testCopySkipsTimestampedReportsSubDirectories() throws Exception {
    File sourceFolder = this.tempFolder.newFolder("reportsFolder");
    File destFolder = this.tempFolder.newFolder("siteFolder");
    String[] destinationFiles;

    new File(sourceFolder, "file0.txt").createNewFile();
    new File(sourceFolder, "file1.txt").createNewFile();
    new File(sourceFolder, "0123456789").createNewFile();

    assertThat(this.fixture.generate(new ReportGenerationContext(null, null,
        sourceFolder, destFolder, this.mockLogger, Arrays
            .asList(HTML_SOURCE_DATA_FORMAT))),
        sameInstance(ReportGenerationResultEnum.SUCCESS));
    assertThat(sourceFolder.list().length, is(3));

    destinationFiles = destFolder.list();
    assertThat(destFolder.listFiles().length, is(2));
    for (String f : destinationFiles) {
      assertThat(f, startsWith("file"));
    }
  }

  @Test
  public void testCopyFails() {
    assertThat(this.fixture.generate(new ReportGenerationContext(null, null,
        new File("foo"), new File("bar"), this.mockLogger, Arrays
            .asList(HTML_SOURCE_DATA_FORMAT))),
        sameInstance(ReportGenerationResultEnum.FAILURE));
  }

  @Test
  public void testSourceDataFormat() {
    assertThat(this.fixture.getGeneratorDataFormat(),
        equalTo(HTML_SOURCE_DATA_FORMAT));
  }

}
