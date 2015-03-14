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
	private Log mockLog;
	
	@Before
	public void setUp() {
		fixture = new ReportSourceLocator();
		mockLog = mock(Log.class);
	}
	
	@Test(expected = PitError.class)
	public void testCouldNotListdirectories() {
		File mockReportsDir = this.buildMockReportsDirectory();
		
		when(mockReportsDir.listFiles(isA(FileFilter.class))).thenReturn(null);
		fixture.locate(mockReportsDir, mockLog);
	}
	
	@Test
	public void testNoSubdirectories() {
		File mockReportsDir = this.buildMockReportsDirectory();
		
		when(mockReportsDir.listFiles(isA(FileFilter.class))).thenReturn(new File[0]);
		assertThat(fixture.locate(mockReportsDir, mockLog), sameInstance(mockReportsDir));
	}
	
	@Test
	public void testOneSubdirectory() {
		File mockReportsDir = this.buildMockReportsDirectory();
		File dummySubDir = mock(File.class);

		when(mockReportsDir.listFiles(isA(FileFilter.class))).thenReturn(new File[]{ dummySubDir });
		when(mockReportsDir.lastModified()).thenReturn(1L);
		when(dummySubDir.lastModified()).thenReturn(2L);
		assertThat(fixture.locate(mockReportsDir, mockLog), sameInstance(dummySubDir));
	}
	
	@Test
	public void testMultipleSubdirectories() {
		File mockReportsDir = this.buildMockReportsDirectory();
		File mockSubDir0 = mock(File.class);
		File mockSubDir1 = mock(File.class);
		File mockSubDir2 = mock(File.class);
		
		when(mockSubDir0.lastModified()).thenReturn(2L);
		when(mockSubDir1.lastModified()).thenReturn(3L);
		when(mockSubDir2.lastModified()).thenReturn(1L);
		
		when(mockReportsDir.listFiles(isA(FileFilter.class))).thenReturn(new File[]{ mockSubDir0, mockSubDir1, mockSubDir2 });
		assertThat(fixture.locate(mockReportsDir, mockLog), sameInstance(mockSubDir1));
	}
	
	@Test(expected = PitError.class)
	public void testNotDirectory() {
		fixture.locate(this.buildMockReportsDirectory(true, true, false), mockLog);
	}
	
	@Test(expected = PitError.class)
	public void testNotReadable() {
		fixture.locate(this.buildMockReportsDirectory(true, false, true), mockLog);
	}
	
	@Test(expected = PitError.class)
	public void testNotExists() {
		fixture.locate(this.buildMockReportsDirectory(false, true, true), mockLog);
	}
	
	private File buildMockReportsDirectory() {
		return this.buildMockReportsDirectory(true, true, true);
	}
	
	private File buildMockReportsDirectory(boolean exists, boolean canRead, boolean isDirectory) {
		File testFile = mock(File.class);
		
		when(testFile.exists()).thenReturn(exists);
		when(testFile.canRead()).thenReturn(canRead);
		when(testFile.isDirectory()).thenReturn(isDirectory);
		
		return testFile;
	}

}
