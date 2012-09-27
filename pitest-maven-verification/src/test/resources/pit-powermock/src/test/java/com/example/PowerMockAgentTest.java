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
package com.example;

import org.junit.Rule;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;

@PrepareForTest({ PowerMockAgentFoo.class })
public class PowerMockAgentTest {

  @Rule
  public PowerMockRule rule = new PowerMockRule();

  @Test
  public void testReplaceStaticCallToOtherClass() {
    PowerMockito.mockStatic(PowerMockAgentFoo.class);

    new PowerMockAgentCallFoo().call();

    PowerMockito.verifyStatic();
    PowerMockAgentFoo.foo();

  }

}
