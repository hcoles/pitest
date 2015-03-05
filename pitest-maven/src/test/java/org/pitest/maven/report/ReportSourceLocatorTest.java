package org.pitest.maven.report;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileFilter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.pitest.util.PitError;

public class ReportSourceLocatorTest {

	private ReportSourceLocator fixture;
	
	@Before
	public void setUp() {
		fixture = new ReportSourceLocator();
	}
	
	@Test(expected = PitError.class)
	public void testCouldNotListdirectories() {
		File mockReportsDir = this.buildMockReportsDirectory();
		
		when(mockReportsDir.listFiles(isA(FileFilter.class))).thenReturn(null);
		fixture.locate(mockReportsDir);
	}
	
	@Test
	public void testNoSubdirectories() {
		File mockReportsDir = this.buildMockReportsDirectory();
		
		when(mockReportsDir.listFiles(isA(FileFilter.class))).thenReturn(new File[0]);
		assertThat(fixture.locate(mockReportsDir), sameInstance(mockReportsDir));
	}
	
	@Test
	public void testOneSubdirectory() {
		File mockReportsDir = this.buildMockReportsDirectory();
		File dummySubDir = new File("");

		when(mockReportsDir.listFiles(isA(FileFilter.class))).thenReturn(new File[]{ dummySubDir });
		assertThat(fixture.locate(mockReportsDir), sameInstance(dummySubDir));
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
		assertThat(fixture.locate(mockReportsDir), sameInstance(mockSubDir2));
	}
	
	@Test
	/**
	 * This test is different than a normal unit test.  The issue here is that the ReportSourceLocator class has a static final 
	 * variable representing the criteria by which a directory is determined to be a timestamped report directory, but there is 
	 * no way to directly test that the criteria is accurate.  Thus, a regular unit test is executed with the directory matcher 
	 * being captured and itself tested.
	 */
	public void testDirectoryMatched() {
		ArgumentCaptor<FileFilter> fileFilterCaptor = ArgumentCaptor.forClass(FileFilter.class);
		File mockReportsDir = this.buildMockReportsDirectory();
		File dummySubDir = new File("");

		when(mockReportsDir.listFiles(fileFilterCaptor.capture())).thenReturn(new File[]{ dummySubDir });
		fixture.locate(mockReportsDir);
		
		//test condition: directory and name is all numbers
		assertThat(this.runDirectoryMatcherTest(fileFilterCaptor.getValue(), true, "20150304"), is(true));
		
		//test condition: not directory and name is all numbers
		assertThat(this.runDirectoryMatcherTest(fileFilterCaptor.getValue(), false, "20150304"), is(false));
		
		//test condition: directory and name is not all numbers
		assertThat(this.runDirectoryMatcherTest(fileFilterCaptor.getValue(), true, "abc20150304"), is(false));
		
		//test condition: not directory and name is not all numbers
		assertThat(this.runDirectoryMatcherTest(fileFilterCaptor.getValue(), false, "abc20150304"), is(false));
	}
	
	@Test(expected = PitError.class)
	public void testNotDirectory() {
		fixture.locate(this.buildMockReportsDirectory(true, true, false));
	}
	
	@Test(expected = PitError.class)
	public void testNotReadable() {
		fixture.locate(this.buildMockReportsDirectory(true, false, true));
	}
	
	@Test(expected = PitError.class)
	public void testNotExists() {
		fixture.locate(this.buildMockReportsDirectory(false, true, true));
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
	
	private boolean runDirectoryMatcherTest(FileFilter fileFilter, boolean isDirectory, String directoryName) {
		File mockDir = mock(File.class);
		
		when(mockDir.isDirectory()).thenReturn(isDirectory);
		when(mockDir.getName()).thenReturn(directoryName);
		
		return fileFilter.accept(mockDir);
	}

}
