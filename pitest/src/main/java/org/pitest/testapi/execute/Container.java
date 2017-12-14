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

package org.pitest.testapi.execute;

import java.util.List;

import org.pitest.testapi.TestResult;
import org.pitest.testapi.TestUnit;

/**
 * This used to be an important concept that allowed tests
 * to be executed in isolation from each other. It now
 * serves almost no purpose but has not yet been fully removed
 * from the codebase.
 *
 */
public interface Container {

  List<TestResult> execute(TestUnit c);

}
