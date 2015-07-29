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

import java.util.Arrays;

import org.pitest.mutationtest.ClassMutationResults;
import org.pitest.mutationtest.MutationMetaData;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.engine.MutationDetails;

public class MutationTestResultMother {

  public static MutationDetails createDetails() {
    return createDetails("file");
  }

  public static MutationDetails createDetails(final String sourceFile) {
    return new MutationDetails(aMutationId().build(), sourceFile, "desc", 42, 0);
  }

  public static MutationMetaData createMetaData(final MutationResult... mrs) {
    return new MutationMetaData(Arrays.asList(mrs));
  }

  public static ClassMutationResults createClassResults(
      final MutationResult... mrs) {
    return new ClassMutationResults(Arrays.asList(mrs));
  }

}
