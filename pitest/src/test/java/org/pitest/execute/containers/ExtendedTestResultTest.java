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
package   org.pitest.execute.containers;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.pitest.execute.containers.ExtendedTestResult;
import org.pitest.functional.Option;
import org.pitest.testapi.MetaData;

public class ExtendedTestResultTest {

  @Test
  public void shouldAllowToAddingAndRetrievalOfMetaData() {
    final MetaData md = new MetaData() {

    };

    final MetaData md2 = new MetaData() {

    };

    final ExtendedTestResult testee = new ExtendedTestResult(null, null, md,
        md2);
    assertEquals(Option.some(md), testee.getValue(md.getClass()));
    assertEquals(Option.some(md2), testee.getValue(md2.getClass()));

  }

  @Test
  public void shouldReturnNoneWhenNoMetaDataOfRequestedTypePresent() {
    final ExtendedTestResult testee = new ExtendedTestResult(null, null);
    assertEquals(Option.none(), testee.getValue(MetaData.class));
  }

}
