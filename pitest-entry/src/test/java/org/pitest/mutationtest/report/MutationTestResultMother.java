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

import static org.pitest.mutationtest.LocationMother.aMutationId;
import static org.pitest.mutationtest.engine.MutationDetailsMother.aMutationDetail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.pitest.coverage.TestInfo;
import org.pitest.mutationtest.ClassMutationResults;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationDetailsMother;
import org.pitest.quickbuilder.Builder;
import org.pitest.quickbuilder.Generator;
import org.pitest.quickbuilder.SequenceBuilder;
import org.pitest.quickbuilder.builders.QB;

public class MutationTestResultMother {

  public interface MutationTestResultBuilder extends SequenceBuilder<MutationResult> {
    MutationTestResultBuilder withMutationDetails(Builder<MutationDetails> details);
    MutationTestResultBuilder withStatusTestPair(MutationStatusTestPair status);

    MutationDetails _MutationDetails();
    MutationStatusTestPair _StatusTestPair();
  }

  public static MutationTestResultBuilder aMutationTestResult() {
    return QB.builder(MutationTestResultBuilder.class, seed())
            .withMutationDetails(aMutationDetail())
            .withStatusTestPair(MutationStatusTestPair.notAnalysed(0, DetectionStatus.SURVIVED));
  }

  private static Generator<MutationTestResultBuilder, MutationResult> seed() {
    return b -> new MutationResult(b._MutationDetails(), b._StatusTestPair());
  }

  public static MutationDetails createDetails() {
    return createDetails("file");
  }

  public static MutationDetails createDetails(final String sourceFile) {
    return new MutationDetails(aMutationId().build(), sourceFile, "desc", 42, 0);
  }

  public static ClassMutationResults createClassResults(
      final MutationResult... mrs) {
    return createClassResults(Arrays.asList(mrs));
  }

  public static ClassMutationResults createClassResults(List<MutationResult> mrs) {
    return new ClassMutationResults(mrs);
  }

}
