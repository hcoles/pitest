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
package org.pitest.junit;

import java.util.Arrays;

import org.pitest.CompoundTestSuiteFinder;
import org.pitest.extension.Configuration;
import org.pitest.extension.TestSuiteFinder;
import org.pitest.extension.TestUnitFinder;

public class JUnitCompatibleConfiguration implements Configuration {

  public TestUnitFinder testUnitFinder() {
    return new JUnitCustomRunnerTestUnitFinder();
  }

  public TestSuiteFinder testSuiteFinder() {
    return new CompoundTestSuiteFinder(Arrays.<TestSuiteFinder> asList(
        new JUnit4SuiteFinder(), new RunnerSuiteFinder()));
  }

}
