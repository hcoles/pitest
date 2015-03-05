package org.pitest.maven.report;

public class XMLReportGenerator implements IReportGenerationStrategy {

	public ReportGenerationResultEnum generate(ReportGenerationContext context) {
		return ReportGenerationResultEnum.NOT_EXECUTED;
	}
	
	public String getGeneratorName() {
		return "XMLReportGenerator";
	}

}
