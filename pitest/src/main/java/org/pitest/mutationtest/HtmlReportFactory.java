/*
 * Copyright 2010 Henry Coles
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

package org.pitest.mutationtest;

import org.pitest.extension.TestListener;
import org.pitest.mutationtest.report.MutationHtmlReportListener;
import org.pitest.mutationtest.report.SmartSourceLocator;

public class HtmlReportFactory implements ListenerFactory {

  public TestListener getListener(final CoverageDatabase coverage,
      final ReportOptions data, final long startTime) {
    return new MutationHtmlReportListener(coverage, startTime,
        data.getReportDir(), new SmartSourceLocator(data.getSourceDirs()));
  }

}
