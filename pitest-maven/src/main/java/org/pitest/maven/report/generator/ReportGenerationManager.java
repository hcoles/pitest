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

import java.util.Arrays;
import java.util.List;

import org.pitest.maven.report.ReportSourceLocator;
import org.pitest.util.PitError;

public class ReportGenerationManager {

  private final ReportSourceLocator            reportLocator;
  private final List<ReportGenerationStrategy> reportGenerationStrategyList;

  public ReportGenerationManager() {
    this(new ReportSourceLocator(), Arrays.asList(new XMLReportGenerator(),
        new HTMLReportGenerator()));
  }

  public ReportGenerationManager(final ReportSourceLocator reportLocator,
      final List<ReportGenerationStrategy> reportGenerationStrategyList) {
    this.reportLocator = reportLocator;
    this.reportGenerationStrategyList = reportGenerationStrategyList;
  }

  public void generateSiteReport(ReportGenerationContext context) {
    ReportGenerationResultEnum result;
    boolean successfulExecution = false;

    context.setReportsDataDirectory(this.reportLocator.locate(
        context.getReportsDataDirectory(), context.getLogger()));

    context.getLogger().debug("starting execution of report generators");
    context.getLogger().debug("using report generation context: " + context);

    for (String dataFormat : context.getSourceDataFormats()) {
      context.getLogger().debug(
          "starting report generator for source data format [" + dataFormat
              + "]");
      result = this.locateReportGenerationStrategy(dataFormat)
          .generate(context);
      context.getLogger().debug(
          "result of report generator for source data format [" + dataFormat
              + "] was [" + result.toString() + "]");
      if (result == ReportGenerationResultEnum.SUCCESS) {
        successfulExecution = true;
        break;
      }
    }

    if (!successfulExecution) {
      throw new PitError("no report generators executed successfully");
    }

    context.getLogger().debug("finished execution of report generators");
  }

  private ReportGenerationStrategy locateReportGenerationStrategy(
      String sourceDataFormat) {
    for (ReportGenerationStrategy strategy : this.reportGenerationStrategyList) {
      if (sourceDataFormat.equalsIgnoreCase(strategy.getGeneratorDataFormat())) {
        return strategy;
      }
    }

    throw new PitError("Could not locate report generator for data source ["
        + sourceDataFormat + "]");
  }

}
