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
package org.pitest.functional;

import static org.junit.Assert.assertEquals;
import static org.pitest.util.Functions.classToName;
import static org.pitest.util.Functions.stringToClass;

import org.junit.Test;

public class OperationsTest {

  @Test
  public void combineShouldPassResultOfFirstFunctionToSecond() {
    final F<Class<?>, Option<Class<?>>> actual = Operations.combine(
        classToName(), stringToClass());
    assertEquals(Option.some(String.class), actual.apply(String.class));

  }

}
