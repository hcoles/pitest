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
package org.pitest.mutationtest.report.csv;

import java.util.Properties;

import org.pitest.mutationtest.ListenerArguments;
import org.pitest.mutationtest.MutationResultListener;
import org.pitest.mutationtest.MutationResultListenerFactory;

public class CSVReportFactory implements MutationResultListenerFactory {

  @Override
  public MutationResultListener getListener(Properties props,
      final ListenerArguments args) {
    return new CSVReportListener(args.getOutputStrategy());
  }

  @Override
  public String name() {
    return "CSV";
  }

  @Override
  public String description() {
    return "Default csv report plugin";
  }

}
