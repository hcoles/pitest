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

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileFilter;

import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.pitest.util.PitError;

public class ReportSourceLocatorTest {

  private ReportSourceLocator fixture;
  private Log                 mockLog;

  @Before
  public void setUp() {
    this.fixture = new ReportSourceLocator();
    this.mockLog = mock(Log.class);
  }

  @Test(expected = PitError.class)
  public void testCouldNotListdirectories() {
    File mockReportsDir = this.buildMockReportsDirectory();

    when(mockReportsDir.listFiles(isA(FileFilter.class))).thenReturn(null);
    this.fixture.locate(mockReportsDir, this.mockLog);
  }

  @Test
  public void testNoSubdirectories() {
    File mockReportsDir = this.buildMockReportsDirectory();

    when(mockReportsDir.listFiles(isA(FileFilter.class))).thenReturn(
        new File[0]);
    assertThat(this.fixture.locate(mockReportsDir, this.mockLog),
        sameInstance(mockReportsDir));
  }

  @Test
  public void testOneSubdirectory() {
    File mockReportsDir = this.buildMockReportsDirectory();
    File dummySubDir = mock(File.class);

    when(mockReportsDir.listFiles(isA(FileFilter.class))).thenReturn(
        new File[] { dummySubDir });
    when(mockReportsDir.lastModified()).thenReturn(1L);
    when(dummySubDir.lastModified()).thenReturn(2L);
    assertThat(this.fixture.locate(mockReportsDir, this.mockLog),
        sameInstance(dummySubDir));
  }

  @Test
  public void testMultipleSubdirectories() {
    File mockReportsDir = this.buildMockReportsDirectory();
    File mockSubDir0 = mock(File.class);
    File mockSubDir1 = mock(File.class);
    File mockSubDir2 = mock(File.class);
    File mockSubDir3 = mock(File.class);

    when(mockSubDir0.lastModified()).thenReturn(2L);
    when(mockSubDir1.lastModified()).thenReturn(3L);
    when(mockSubDir2.lastModified()).thenReturn(1L);
    when(mockSubDir3.lastModified()).thenReturn(3L);

    when(mockReportsDir.listFiles(isA(FileFilter.class))).thenReturn(
        new File[] { mockSubDir0, mockSubDir1, mockSubDir2, mockSubDir3 });
    assertThat(this.fixture.locate(mockReportsDir, this.mockLog),
        sameInstance(mockSubDir1));
  }

  @Test(expected = PitError.class)
  public void testNotDirectory() {
    this.fixture.locate(this.buildMockReportsDirectory(true, true, false),
        this.mockLog);
  }

  @Test(expected = PitError.class)
  public void testNotReadable() {
    this.fixture.locate(this.buildMockReportsDirectory(true, false, true),
        this.mockLog);
  }

  @Test(expected = PitError.class)
  public void testNotExists() {
    this.fixture.locate(this.buildMockReportsDirectory(false, true, true),
        this.mockLog);
  }

  private File buildMockReportsDirectory() {
    return this.buildMockReportsDirectory(true, true, true);
  }

  private File buildMockReportsDirectory(boolean exists, boolean canRead,
      boolean isDirectory) {
    File testFile = mock(File.class);

    when(testFile.exists()).thenReturn(exists);
    when(testFile.canRead()).thenReturn(canRead);
    when(testFile.isDirectory()).thenReturn(isDirectory);

    return testFile;
  }

}
