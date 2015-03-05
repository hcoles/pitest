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

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

/**
 * Generates a report of the pit mutation testing.
 * 
 * @goal report
 * @phase site
 */
public class PitReportMojo extends AbstractMavenReport {

	//TODO what about the exportLineCoverage option?
	//TODO add getters and setters for each field
	
    /**
     * @component
     * @required
     * @readonly
     */
    private Renderer siteRenderer;
    
    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
    
    /**
     * When set indicates that analysis of this project should be skipped
     * 
     * @parameter default-value="false"
     */
    private boolean skip;
    
    /**
     * Base directory where all reports are written to.
     * 
     * @parameter default-value="${project.build.directory}/pit-reports"
     *            expression="${reportsDirectory}"
     */
    private File reportsDirectory;
    
	public String getOutputName() {
		return "pitest/index";
	}

	public String getName(Locale locale) {
		//TODO internationalize this
		return "Pitest Report";
	}

	public String getDescription(Locale locale) {
		//TODO internationalize this
		return "Report of the pitest coverage";
	}

	@Override
	protected Renderer getSiteRenderer() {
		return this.siteRenderer;
	}

	@Override
	protected String getOutputDirectory() {
		return this.reportsDirectory.getAbsolutePath();
	}

	@Override
	protected MavenProject getProject() {
		return this.project;
	}

	@Override
	protected void executeReport(Locale locale) throws MavenReportException {
		this.getLog().warn("PitReportMojo - starting");
		
		if(!this.reportsDirectory.exists()){
			throw new RuntimeException("could not find reports directory [" + this.reportsDirectory + "]");
		}
		
		if(!this.reportsDirectory.canRead()){
			throw new RuntimeException("reports directory [" + this.reportsDirectory + "] not readable");
		}
		
		if(!this.reportsDirectory.isDirectory()){
			throw new RuntimeException("reports directory [" + this.reportsDirectory + "] is actually a file, it must be a directory");
		}
		
		this.executeReportGenerators(
				Arrays.asList(new XMLReportGenerator(), new HTMLReportGenerator()), 
				new ReportGenerationContext(locale, this.getSink(), this.reportsDirectory, new File(this.getReportOutputDirectory() + File.separator + "pitest"), this.getLog())
		);
		
		this.getLog().warn("PitReportMojo - ending");
	}
	
	@Override
	public boolean canGenerateReport() {
		//TODO update to log if report will not be generated
		return !skip;
	}
	
	public boolean isExternalReport() {
	    return true;
	}
	
	private void executeReportGenerators(List<IReportGenerationStrategy> generators, ReportGenerationContext context) {
		ReportGenerationResultEnum result;
		boolean successfulExecution = false;
		
		this.getLog().info("starting execution of report generators");
		this.getLog().info("using report generation context: " + context);
		
		for(IReportGenerationStrategy generator : generators){
			this.getLog().info("starting report generator " + generator.getGeneratorName());
			result = generator.generate(context);
			this.getLog().info("result of report generator was: " + result.toString());
			if(result == ReportGenerationResultEnum.SUCCESS){
				successfulExecution = true;
				break;
			}
			this.getLog().info("starting report generator " + generator.getGeneratorName());
		}
		
		if(!successfulExecution){
			throw new RuntimeException("no report generators executed successfully");
		}
		
		this.getLog().info("finished execution of report generators");
	}
	
}
