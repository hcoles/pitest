/*
 * Copyright 2014 Artem Khvastunov
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
package org.pitest.mutationtest.engine.gregor;

import static org.junit.Assert.assertEquals;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.pitest.functional.predicate.True;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.config.Mutator;
import org.pitest.mutationtest.engine.gregor.inlinedcode.InlinedCodeFilter;
import org.pitest.mutationtest.engine.gregor.inlinedcode.InlinedFinallyBlockDetector;
import org.pitest.util.ResourceFolderByteArraySource;

/**
 * @author Artem Khvastunov &lt;contact@artspb.me&gt;
 */
@RunWith(Theories.class)
public class TestTryWithResources extends MutatorTestBase {

  private static final Collection<String> COMPILERS = Arrays.asList("javac",
                                                        "ecj", "aspectj");
  private static final String             PATH      = "trywithresources/{0}_{1}";
  private static final String             MESSAGE   = "class={0}, compiler={1}";

  @DataPoints
  public static String[][]                data      = new String[][] {
      { "1", "TryExample" }, { "2", "TryCatchExample" },
      { "3", "TryCatchFinallyExample" }, { "2", "TryFinallyExample" },
      { "1", "TryWithTwoCloseableExample" },
      { "1", "TryWithNestedTryExample" }, { "1", "TryWithInterfaceExample" } };

  @Theory
  public void testTryWithResourcesMutationsWithFilter(String... data) {
    createEngine(new InlinedFinallyBlockDetector());
    testWithExpected(data[0], data[1]);
  }

  private void createEngine(InlinedCodeFilter inlinedCodeDetector) {
    this.engine = new GregorMutater(new ResourceFolderByteArraySource(),
        True.<MethodInfo> all(), Mutator.defaults(),
        Collections.<String> emptyList(), inlinedCodeDetector);
  }

  private void testWithExpected(String expected, String className) {
    for (String compiler : COMPILERS) {
      String clazz = MessageFormat.format(PATH, className, compiler);
      final Collection<MutationDetails> actualDetails = findMutationsFor(clazz);
      String message = MessageFormat.format(MESSAGE, className, compiler);
      assertEquals(message, Long.valueOf(expected),
          Long.valueOf(actualDetails.size()));
    }
  }
}
