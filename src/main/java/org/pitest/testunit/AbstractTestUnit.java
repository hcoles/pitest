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
package org.pitest.testunit;

import org.pitest.Description;
import org.pitest.extension.ResultCollector;
import org.pitest.extension.TestUnit;
import org.pitest.functional.Option;

/**
 * @author henry
 * 
 */
public abstract class AbstractTestUnit implements TestUnit {

  private static final long serialVersionUID = 1L;

  private final Description description;
  // private final Configuration configuration;
  private Option<TestUnit>  dependsOn;

  public AbstractTestUnit(final Description description,
      final TestUnit dependsOn) {
    this.description = description;
    // this.configuration = config;
    this.dependsOn = Option.someOrNone(dependsOn);
  }

  public abstract void execute(final ClassLoader loader,
      final ResultCollector rc);

  public final Description description() {
    return this.description;
  }

  // public Configuration configuration() {
  // return this.configuration;
  // }

  public Option<TestUnit> dependsOn() {
    return this.dependsOn;
  }

  public void setDependency(final TestUnit tu) {
    this.dependsOn = Option.someOrNone(tu);
  }

}
