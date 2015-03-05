package org.pitest.maven.report;

import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class HTMLReportGenerator implements IReportGenerationStrategy {

	public ReportGenerationResultEnum generate(ReportGenerationContext context) {
		try {
			context.getLogger().info(this.getClass().getSimpleName() + " using directory [" + context.getReportsDirectory() + "] as directory containing the html report");
			context.getLogger().info(this.getClass().getSimpleName() + " using directory [" + context.getSiteDirectory() + "] as directory that is the destination of the site report");
			FileUtils.copyDirectory(context.getReportsDirectory(), context.getSiteDirectory());
		} catch (IOException e) {
			context.getLogger().warn(e);
			return ReportGenerationResultEnum.FAILURE;
		}
		
		return ReportGenerationResultEnum.SUCCESS;
	}
	
	public String getGeneratorName() {
		return "HTMLReportGenerator";
	}
	
}
