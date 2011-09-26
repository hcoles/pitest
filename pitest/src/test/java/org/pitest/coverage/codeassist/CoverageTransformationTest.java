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

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.boot.CodeCoverageStore;
import org.pitest.boot.InvokeReceiver;
import org.pitest.coverage.codeassist.samples.ClassWithAMethod;
import org.pitest.coverage.codeassist.samples.ClassWithInitialisedField;

public class CoverageTransformationTest {

  private static final int       CLASS_WITH_METHOD_DEFAULT_CONS_LINE = 17;
  private static final int       CLASS_WITH_METHOD_METHOD_LINE       = 20;
  private static final int       FIRST_CLASS                         = 0;
  private CoverageTransformation testee;

  @Mock
  private InvokeReceiver         invokeQueue;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    this.testee = new CoverageTransformation();
    CodeCoverageStore.resetClassCounter();
    CodeCoverageStore.init(this.invokeQueue);
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

    verify(this.invokeQueue).addCodelineInvoke(FIRST_CLASS,
        CLASS_WITH_METHOD_DEFAULT_CONS_LINE);
    verify(this.invokeQueue).addCodelineInvoke(FIRST_CLASS,
        CLASS_WITH_METHOD_METHOD_LINE);

  }

  @Test
  public void willRecordMultipleVisitsToTheSameLine() throws Exception {
    final String sampleName = ClassWithAMethod.class.getName();
    final byte[] bytes = this.testee.transform(sampleName,
        ClassUtils.classAsBytes(sampleName));

    final Class<?> clazz = ClassUtils.createClass(bytes);
    final Object instance = clazz.newInstance();
    final Method method = clazz.getMethod("aMethod");
    method.invoke(instance);
    method.invoke(instance);

    verify(this.invokeQueue).addCodelineInvoke(FIRST_CLASS,
        CLASS_WITH_METHOD_DEFAULT_CONS_LINE);
    verify(this.invokeQueue, times(2)).addCodelineInvoke(FIRST_CLASS,
        CLASS_WITH_METHOD_METHOD_LINE);

  }

  @Test
  public void shouldRecordVisitedLinesInInitialisedField() throws Exception {
    final String sampleName = ClassWithInitialisedField.class.getName();

    final byte[] bytes = this.testee.transform(sampleName,
        ClassUtils.classAsBytes(sampleName));

    final Class<?> clazz = ClassUtils.createClass(bytes);
    clazz.newInstance();

    verify(this.invokeQueue, atLeastOnce()).addCodelineInvoke(FIRST_CLASS, 20);
    verify(this.invokeQueue, atLeastOnce()).addCodelineInvoke(FIRST_CLASS, 22);

  }

}
