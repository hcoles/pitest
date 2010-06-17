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
package org.pitest.extension.common;

import static org.junit.Assert.fail;

import org.apache.commons.lang.SerializationUtils;
import org.junit.Test;

public class TimeoutDecoratorTest {

  private TimeoutDecorator testee;

  @Test
  public void testCanBeSerializedAndDeserialized() throws Exception {
    try {
      this.testee = new TimeoutDecorator(null, 1);
      SerializationUtils.clone(this.testee);
    } catch (final Throwable t) {
      fail();
    }
  }

}
