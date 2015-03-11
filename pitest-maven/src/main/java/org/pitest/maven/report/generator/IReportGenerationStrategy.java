package org.pitest.maven.report.generator;



public interface IReportGenerationStrategy {

	public ReportGenerationResultEnum generate(ReportGenerationContext context);
	public String getGeneratorName();
	
}
