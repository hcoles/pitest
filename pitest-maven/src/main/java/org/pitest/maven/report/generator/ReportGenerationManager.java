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

import java.util.LinkedList;
import java.util.List;

import org.pitest.maven.report.ReportSourceLocator;
import org.pitest.util.PitError;

public class ReportGenerationManager {

	protected ReportSourceLocator reportLocator;
	protected List<IReportGenerationStrategy> reportGenerationStrategyList;
	
	public ReportGenerationManager() {
		this.reportLocator = new ReportSourceLocator();
		
		this.reportGenerationStrategyList = new LinkedList<IReportGenerationStrategy>();
    	this.reportGenerationStrategyList.add(new XMLReportGenerator());
    	this.reportGenerationStrategyList.add(new HTMLReportGenerator());
	}
	
	public void generateSiteReport(ReportGenerationContext context) {
		ReportGenerationResultEnum result;
		boolean successfulExecution = false;
		
		context.setReportsDataDirectory(this.reportLocator.locate(context.getReportsDataDirectory(), context.getLogger()));
		
		context.getLogger().debug("starting execution of report generators");
		context.getLogger().debug("using report generation context: " + context);
		
		for(IReportGenerationStrategy generator : this.reportGenerationStrategyList){
			context.getLogger().debug("starting report generator [" + generator.getGeneratorName() + "]");
			result = generator.generate(context);
			context.getLogger().debug("result of report generator [" + generator.getGeneratorName() + "] was [" + result.toString() + "]");
			if(result == ReportGenerationResultEnum.SUCCESS){
				successfulExecution = true;
				break;
			}
		}
		
		if(!successfulExecution){
			throw new PitError("no report generators executed successfully");
		}
		
		context.getLogger().debug("finished execution of report generators");
	}
	
}
