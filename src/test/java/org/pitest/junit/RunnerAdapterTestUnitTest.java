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

import static org.junit.Assert.assertSame;

import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pitest.TestGroup;
import org.pitest.extension.TestUnit;

import com.example.TheoryTest;
import com.thoughtworks.xstream.XStream;

public class RunnerAdapterTestUnitTest {

  private RunnerAdapter adapter;

  @Before
  public void setup() {
    this.adapter = new RunnerAdapter(TheoryTest.class);
  }

  @Test
  public void testXStreamSerializationDoesNotDuplicateAdapter() {
    final List<TestUnit> tus = this.adapter.getTestUnits();
    final TestGroup group = new TestGroup();
    for (final TestUnit each : tus) {
      group.add(each);
    }

    final XStream xstream = new XStream();
    final String xml = xstream.toXML(group);

    final XStream xstream2 = new XStream();
    final TestGroup actual = (TestGroup) xstream2.fromXML(xml);

    final Iterator<TestUnit> it = actual.iterator();
    RunnerAdapterTestUnit tu = (RunnerAdapterTestUnit) it.next();
    final RunnerAdapter firstAdapter = tu.getAdapter();
    tu = (RunnerAdapterTestUnit) it.next();
    final RunnerAdapter secondAdapter = tu.getAdapter();

    assertSame(firstAdapter, secondAdapter);

  }

}
