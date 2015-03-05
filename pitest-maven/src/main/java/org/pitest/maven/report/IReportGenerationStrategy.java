package org.pitest.maven.report;

public interface IReportGenerationStrategy {

	public ReportGenerationResultEnum generate(ReportGenerationContext context);
	public String getGeneratorName();
	
}
