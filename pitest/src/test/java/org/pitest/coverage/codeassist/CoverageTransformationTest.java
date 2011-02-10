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
package org.pitest.coverage.codeassist;

import static junit.framework.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pitest.coverage.CodeCoverageStore;
import org.pitest.coverage.CoverageStatistics;
import org.pitest.coverage.InvokeEntry;
import org.pitest.coverage.InvokeQueue;
import org.pitest.coverage.codeassist.samples.ClassWithAMethod;
import org.pitest.coverage.codeassist.samples.ClassWithInitialisedField;

public class CoverageTransformationTest {

  private static final int       CLASS_WITH_METHOD_DEFAULT_CONS_LINE = 17;
  private static final int       CLASS_WITH_METHOD_METHOD_LINE       = 20;
  private static final int       FIRST_CLASS                         = 0;
  private CoverageTransformation testee;
  private CoverageStatistics     invokeStatistics;
  private InvokeQueue            invokeQueue;

  @Before
  public void setup() {
    this.testee = new CoverageTransformation();
    this.invokeStatistics = new CoverageStatistics();
    this.invokeQueue = new InvokeQueue();
    CodeCoverageStore.init(this.invokeQueue, this.invokeStatistics);
  }

  @Test
  public void shouldRecordVisitedLinesInMethod() throws Exception {
    final String sampleName = ClassWithAMethod.class.getName();
    final byte[] bytes = this.testee.transform(sampleName,
        ClassUtils.classAsBytes(sampleName));

    final Class<?> clazz = ClassUtils.createClass(bytes);
    final Object instance = clazz.newInstance();
    final Method method = clazz.getMethod("aMethod");
    method.invoke(instance);

    final List<InvokeEntry> actual = getRecordedLines();
    final List<InvokeEntry> expected = Arrays.asList(
        line(CLASS_WITH_METHOD_DEFAULT_CONS_LINE),
        line(CLASS_WITH_METHOD_METHOD_LINE));
    assertEquals(expected, actual);

  }

  @Test
  public void shouldRecordMultipleVisitsToTheSameLine() throws Exception {
    final String sampleName = ClassWithAMethod.class.getName();
    final byte[] bytes = this.testee.transform(sampleName,
        ClassUtils.classAsBytes(sampleName));

    final Class<?> clazz = ClassUtils.createClass(bytes);
    final Object instance = clazz.newInstance();
    final Method method = clazz.getMethod("aMethod");
    method.invoke(instance);
    method.invoke(instance);

    final List<InvokeEntry> actual = getRecordedLines();
    final List<InvokeEntry> expected = Arrays.asList(
        line(CLASS_WITH_METHOD_DEFAULT_CONS_LINE),
        line(CLASS_WITH_METHOD_METHOD_LINE),
        line(CLASS_WITH_METHOD_METHOD_LINE));
    assertEquals(expected, actual);
  }

  @Test
  public void shouldRecordVisitedLinesInInitialisedField() throws Exception {
    final String sampleName = ClassWithInitialisedField.class.getName();

    final byte[] bytes = this.testee.transform(sampleName,
        ClassUtils.classAsBytes(sampleName));

    final Class<?> clazz = ClassUtils.createClass(bytes);
    clazz.newInstance();

    final List<InvokeEntry> actual = getRecordedLines();
    // we seem to record two visits to the deafault constructor
    // this is not a problem but wold be nice to properly understand
    final List<InvokeEntry> expected = Arrays.asList(line(20), line(22),
        line(20));
    assertEquals(expected, actual);
  }

  private InvokeEntry line(final int line) {
    return new InvokeEntry(FIRST_CLASS, line);
  }

  private List<InvokeEntry> getRecordedLines() throws InterruptedException {
    final InvokeQueue queue = CodeCoverageStore.getInvokeQueue();
    final List<InvokeEntry> ies = new ArrayList<InvokeEntry>();
    while (!queue.isEmpty()) {
      ies.addAll(queue.poll(100));
    }
    return ies;

  }

}
