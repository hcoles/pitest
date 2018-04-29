/*
 * Copyright 2015 Jason Fehr
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
package org.pitest.maven.report;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ReportSourceLocatorParamaterizedTest {

  private static final String VALID_DIRECTORY_NAME   = "20150304";
  private static final String INVALID_DIRECTORY_NAME = "abc2015def0304gh";

  @Parameters(name = "{index}: reportSourceLocator(isDirectory: {0}, directoryName: \"{1}\", canWrite: {2}, shouldBeValid: {3})")
  public static final Iterable<Object[]> data() {
    return Arrays.asList(new Object[][] {
        { true, VALID_DIRECTORY_NAME, true, true },
        { true, VALID_DIRECTORY_NAME, false, false },
        { true, INVALID_DIRECTORY_NAME, true, false },
        { true, INVALID_DIRECTORY_NAME, false, false },
        { false, VALID_DIRECTORY_NAME, true, false },
        { false, VALID_DIRECTORY_NAME, false, false },
        { false, INVALID_DIRECTORY_NAME, true, false },
        { false, INVALID_DIRECTORY_NAME, false, false } });
  }

  @Parameter
  public boolean isDirectory;

  @Parameter(value = 1)
  public String  directoryName;

  @Parameter(value = 2)
  public boolean canWrite;

  @Parameter(value = 3)
  public boolean expectedResult;

  @Test
  public void test() {
    File mockDir = mock(File.class);

    when(mockDir.isDirectory()).thenReturn(this.isDirectory);
    when(mockDir.getParentFile()).thenReturn(null);
    when(mockDir.getName()).thenReturn(this.directoryName);
    when(mockDir.canWrite()).thenReturn(this.canWrite);

    assertThat(
        ReportSourceLocator.TIMESTAMPED_REPORTS_FILE_FILTER.accept(mockDir),
        is(this.expectedResult));
  }

}
