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
package org.pitest.maven.report.generator;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Arrays;

import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;

public class XMLReportGeneratorTest {

  private static final String XML_SOURCE_DATA_FORMAT = "XML";

  private Log                 mockLogger;
  private XMLReportGenerator  fixture;

  @Before
  public void setUp() {
    this.mockLogger = mock(Log.class);
    this.fixture = new XMLReportGenerator();
  }

  @Test
  public void testName() {
    assertThat(this.fixture.getGeneratorName(), is("XMLReportGenerator"));
  }

  @Test
  public void testNotExecuted() {
    assertThat(this.fixture.generate(new ReportGenerationContext(null, null,
        null, null, this.mockLogger, Arrays.asList(XML_SOURCE_DATA_FORMAT))),
        sameInstance(ReportGenerationResultEnum.NOT_EXECUTED));
  }

  @Test
  public void testSourceDataFormat() {
    assertThat(this.fixture.getGeneratorDataFormat(),
        equalTo(XML_SOURCE_DATA_FORMAT));
  }

}
