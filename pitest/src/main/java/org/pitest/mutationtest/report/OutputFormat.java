/*
 * Copyright 2011 Henry Coles
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
package org.pitest.mutationtest.report;

import org.pitest.functional.F;
import org.pitest.mutationtest.HtmlReportFactory;
import org.pitest.mutationtest.ListenerFactory;

/**
 * Quick and dirty list of valid output format types
 */
public enum OutputFormat {
  HTML(HtmlReportFactory.createFactoryFunction()), CSV(CSVReportFactory // NO_UCD
      .createFactoryFunction()), XML(XMLReportFactory.createFactoryFunction()); // NO_UCD

  private final F<ResultOutputStrategy, ListenerFactory> createFactory;

  private OutputFormat(
      final F<ResultOutputStrategy, ListenerFactory> createFactory) {
    this.createFactory = createFactory;
  }

  public ListenerFactory createFactory(final ResultOutputStrategy output) {
    return this.createFactory.apply(output);
  }

  public static F<OutputFormat, ListenerFactory> createFactoryForFormat(
      final ResultOutputStrategy outputStrategy) {
    return new F<OutputFormat, ListenerFactory>() {

      public ListenerFactory apply(final OutputFormat format) {
        return format.createFactory(outputStrategy);
      }

    };
  }
}
