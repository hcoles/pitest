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

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.pitest.containers.UnisolatedThreadPoolContainer;
import org.pitest.extension.Configuration;
import org.pitest.extension.Container;
import org.pitest.extension.InstantiationStrategy;
import org.pitest.extension.TestSuiteFinder;
import org.pitest.extension.TestUnitFinder;
import org.pitest.extension.TestUnitProcessor;

/**
 * @author henry
 * 
 */
public class EmptyConfiguration implements Configuration {

  public InstantiationStrategy instantiationStrategy() {
    return new NoArgsConstructorInstantiationStrategy();
  }

  public Container container() {
    return new UnisolatedThreadPoolContainer(1);
  }

  public int maxParallelism() {
    return 1;
  }

  public Set<TestUnitProcessor> testUnitProcessors() {
    return Collections.emptySet();
  }

  public Set<TestUnitFinder> testUnitFinders() {
    return Collections.emptySet();
  }

  public boolean allowConfigurationChange() {
    return true;
  }

  public Collection<TestSuiteFinder> testSuiteFinders() {
    return Collections.emptySet();
  }

}
