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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pitest.maven.report.ReportSourceLocator;
import org.pitest.util.PitError;

@RunWith(MockitoJUnitRunner.class)
public class ReportGenerationManagerTest {

	private ReportGenerationContext generationContext;
	private List<IReportGenerationStrategy> reportGenerationStrategyList;
	
	@Mock private ReportSourceLocator reportLocator;
	@Mock private XMLReportGenerator xmlGenerator;
	@Mock private HTMLReportGenerator htmlGenerator;
	@Mock private Log log;
	@Mock private File reportsDataDirectory;
	@Mock private File siteDirectory;
	
	@InjectMocks private ReportGenerationManager fixture;
	
	@Before
	public void setUp() {
		this.reportGenerationStrategyList = new LinkedList<IReportGenerationStrategy>();
		this.reportGenerationStrategyList.add(xmlGenerator);
		this.reportGenerationStrategyList.add(htmlGenerator);
		this.fixture.reportGenerationStrategyList = this.reportGenerationStrategyList;
		
		this.generationContext = new ReportGenerationContext(Locale.ENGLISH, null, this.reportsDataDirectory, this.siteDirectory, this.log);
	}
	
	@Test
	public void testFirstGeneratorSuccess() {
		when(this.xmlGenerator.generate(this.generationContext)).thenReturn(ReportGenerationResultEnum.SUCCESS);
		
		this.fixture.generateSiteReport(this.generationContext);
		
		verify(this.xmlGenerator).generate(this.generationContext);
		verifyZeroInteractions(this.htmlGenerator);
	}
	
	@Test
	public void testFirstGeneratorNotRunSecondGeneratorSuccess() {
		when(this.xmlGenerator.generate(this.generationContext)).thenReturn(ReportGenerationResultEnum.NOT_EXECUTED);
		when(this.htmlGenerator.generate(this.generationContext)).thenReturn(ReportGenerationResultEnum.SUCCESS);
		
		this.fixture.generateSiteReport(this.generationContext);
		
		verify(this.xmlGenerator).generate(this.generationContext);
		verify(this.htmlGenerator).generate(this.generationContext);
	}
	
	@Test(expected = PitError.class)
	public void testFirstGeneratorNotRunSecondGeneratorNotRun() {
		when(this.xmlGenerator.generate(this.generationContext)).thenReturn(ReportGenerationResultEnum.NOT_EXECUTED);
		when(this.htmlGenerator.generate(this.generationContext)).thenReturn(ReportGenerationResultEnum.NOT_EXECUTED);
		
		this.fixture.generateSiteReport(this.generationContext);
	}
	
	@Test
	public void testFirstGeneratorFailSecondGeneratorSuccess() {
		when(this.xmlGenerator.generate(this.generationContext)).thenReturn(ReportGenerationResultEnum.FAILURE);
		when(this.htmlGenerator.generate(this.generationContext)).thenReturn(ReportGenerationResultEnum.SUCCESS);
		
		this.fixture.generateSiteReport(this.generationContext);
		
		verify(this.xmlGenerator).generate(this.generationContext);
		verify(this.htmlGenerator).generate(this.generationContext);
	}
	
	@Test(expected = PitError.class)
	public void testFirstGeneratorFailSecondGeneratorFail() {
		when(this.xmlGenerator.generate(this.generationContext)).thenReturn(ReportGenerationResultEnum.FAILURE);
		when(this.htmlGenerator.generate(this.generationContext)).thenReturn(ReportGenerationResultEnum.FAILURE);
		
		this.fixture.generateSiteReport(this.generationContext);
	}

}
