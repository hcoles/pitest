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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.junit.Before;
import org.junit.Test;

import com.example.CoveredByEasyMock.AnInterface;

public class EasyMockTest {

  private CoveredByEasyMock testee;
  private AnInterface       mock;

  @Before
  public void setUp() {
    this.mock = createMock(AnInterface.class);
    this.testee = new CoveredByEasyMock();
  }

  @Test
  public void testAddDocument() {
    this.mock.callMe();
    expectLastCall().times(2);

    replay(this.mock);
    this.testee.doStuff(this.mock);
    verify(this.mock);
  }

}
