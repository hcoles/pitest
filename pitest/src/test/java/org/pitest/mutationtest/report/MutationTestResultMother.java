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

import java.util.Arrays;
import java.util.Collections;

import org.pitest.DescriptionMother;
import org.pitest.ExtendedTestResult;
import org.pitest.TestResult;
import org.pitest.mutationtest.MethodName;
import org.pitest.mutationtest.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.instrument.MutationMetaData;
import org.pitest.mutationtest.results.MutationResult;

public class MutationTestResultMother {

  public static MutationDetails createDetails() {
    return createDetails("file");
  }

  public static MutationDetails createDetails(final String sourceFile) {
    return new MutationDetails(new MutationIdentifier("class", 1, "mutator"),
        sourceFile, "desc", new MethodName("method"), 42, 0);
  }

  public static MutationMetaData createMetaData(final MutationResult... mrs) {
    return new MutationMetaData(Collections.<String> emptyList(),
        Arrays.asList(mrs));
  }

  public static TestResult createResult(final MutationMetaData md) {
    return new ExtendedTestResult(
        DescriptionMother.createEmptyDescription("foo"), null, md);
  }

}
