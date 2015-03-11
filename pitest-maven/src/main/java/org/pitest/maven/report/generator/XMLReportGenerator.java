package org.pitest.maven.report.generator;


public class XMLReportGenerator implements IReportGenerationStrategy {

	public ReportGenerationResultEnum generate(ReportGenerationContext context) {
		return ReportGenerationResultEnum.NOT_EXECUTED;
	}
	
	public String getGeneratorName() {
		return "XMLReportGenerator";
	}

}
