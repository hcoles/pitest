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

import java.io.IOException;
import java.io.Writer;

import org.pitest.functional.Option;
import org.pitest.mutationtest.ClassMutationResults;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationResultListener;
import org.pitest.util.ResultOutputStrategy;
import org.pitest.util.Unchecked;

public class CSVReportListener implements MutationResultListener {

  private final Writer out;

  public CSVReportListener(final ResultOutputStrategy outputStrategy) {
    this(outputStrategy.createWriterForFile("mutations.csv"));
  }

  public CSVReportListener(final Writer out) {
    this.out = out;
  }

  private String createKillingTestDesc(final Option<String> killingTest) {
    if (killingTest.hasSome()) {
      return killingTest.value();
    } else {
      return "none";
    }
  }

  private String makeCsv(final Object... os) {
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i != os.length; i++) {
      sb.append(os[i].toString());
      if (i != (os.length - 1)) {
        sb.append(",");
      }
    }
    return sb.toString();
  }

  @Override
  public void runStart() {

  }

  @Override
  public void runEnd() {
    try {
      this.out.close();
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

  @Override
  public void handleMutationResult(final ClassMutationResults metaData) {
    try {

      for (final MutationResult mutation : metaData.getMutations()) {
        this.out.write(makeCsv(mutation.getDetails().getFilename(), mutation
            .getDetails().getClassName().asJavaName(), mutation.getDetails()
            .getMutator(), mutation.getDetails().getMethod(), mutation
            .getDetails().getLineNumber(), mutation.getStatus(),
            createKillingTestDesc(mutation.getKillingTest()))
            + System.getProperty("line.separator"));
      }

    } catch (final IOException ex) {
      throw Unchecked.translateCheckedException(ex);
    }

  }

}
