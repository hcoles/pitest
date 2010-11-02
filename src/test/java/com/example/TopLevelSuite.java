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
package com.example;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Executors;

import org.junit.runner.RunWith;
import org.pitest.DefaultStaticConfig;
import org.pitest.annotations.PITContainer;
import org.pitest.annotations.PITSuite;
import org.pitest.annotations.StaticConfigurationClass;
import org.pitest.containers.BaseThreadPoolContainer;
import org.pitest.distributed.DistributedContainer;
import org.pitest.extension.Container;
import org.pitest.extension.IsolationStrategy;
import org.pitest.extension.TestListener;
import org.pitest.extension.Transformation;
import org.pitest.extension.common.AllwaysIsolateStrategy;
import org.pitest.extension.common.ConsoleResultListener;
import org.pitest.internal.TransformingClassLoaderFactory;
import org.pitest.internal.transformation.IdentityTransformation;
import org.pitest.junit.adapter.PITJUnitRunner;
import org.pitest.mutationtest.MutationTest;
import org.pitest.mutationtest.Mutator;

@RunWith(PITJUnitRunner.class)
@MutationTest(threshold = 66, mutators = { Mutator.INCREMENTS,
    Mutator.RETURN_VALS })
@StaticConfigurationClass(TopLevelSuite.class)
public class TopLevelSuite extends DefaultStaticConfig {

  @PITContainer
  public static Container isolated() {

    final IsolationStrategy i = new AllwaysIsolateStrategy();
    final Transformation t = new IdentityTransformation();
    return new BaseThreadPoolContainer(2, new TransformingClassLoaderFactory(t,
        i), Executors.defaultThreadFactory());
  }

  // @PITContainer
  public static Container distributed() {
    return new DistributedContainer();
  }

  @PITSuite
  public static Collection<Class<?>> children() {
    return Arrays.<Class<?>> asList(JUnit4SuiteA.class, JUnit4SuiteB.class);
  }

  @Override
  public Collection<TestListener> getTestListeners() {
    return Collections.<TestListener> singletonList(ConsoleResultListener
        .instance());
  }

}
