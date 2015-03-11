package org.pitest.maven.report.generator;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class HTMLReportGeneratorTest {

	@Rule public TemporaryFolder tempFolder = new TemporaryFolder();
	
	private Log mockLogger;
	private HTMLReportGenerator fixture;
	
	@Before
	public void setUp() {
		mockLogger = mock(Log.class);
		fixture = new HTMLReportGenerator();
	}
	
	@Test
	public void testName() {
		assertThat(fixture.getGeneratorName(), equalTo("HTMLReportGenerator"));
	}
	
	@Test
	public void testCopySuccess() throws IOException {
		File sourceFolder = tempFolder.newFolder("reportsFolder");
		File destFolder = tempFolder.newFolder("siteFolder");
		File sourceFile0 = tempFolder.newFile("file0.txt");
		File sourceFile1 = tempFolder.newFile("file1.txt");

		sourceFile0 = new File(sourceFolder, "file0.txt");
		sourceFile0.createNewFile();
		
		sourceFile1 = new File(sourceFolder, "file1.txt");
		sourceFile1.createNewFile();
		System.out.println(sourceFile0.getAbsolutePath());
		
		assertThat(fixture.generate(new ReportGenerationContext(null, null, sourceFile0.getParentFile(), destFolder, mockLogger)), sameInstance(ReportGenerationResultEnum.SUCCESS));
		assertThat(sourceFolder.listFiles().length, is(2));
		assertThat(destFolder.listFiles().length, is(2));
	}
	
	@Test
	public void testCopyFails() {
		assertThat(fixture.generate(new ReportGenerationContext(null, null, new File("foo"), new File("bar"), mockLogger)), sameInstance(ReportGenerationResultEnum.FAILURE));
	}
	
}
