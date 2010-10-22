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
package org.pitest.mutationtest;

import java.util.Collections;
import java.util.List;

import org.pitest.Description;
import org.pitest.Pitest;
import org.pitest.extension.Configuration;
import org.pitest.extension.TestDiscoveryListener;
import org.pitest.extension.TestUnit;
import org.pitest.testunit.AbstractTestUnit;

public abstract class AbstractMutationTestUnit extends AbstractTestUnit {

  protected final Class<?>       test;
  protected final Class<?>       classToMutate;
  protected final MutationConfig config;
  protected final Configuration  pitConfig;

  public static String randomFilename() {
    return System.currentTimeMillis()
        + ("" + Math.random()).replaceAll("\\.", "");
  }

  public AbstractMutationTestUnit(final Class<?> test,
      final Class<?> classToMutate, final MutationConfig mutationConfig,
      final Configuration pitConfig, final Description description) {
    super(description, null);
    this.classToMutate = classToMutate;
    this.test = test;
    this.config = mutationConfig;
    this.pitConfig = pitConfig;

  }

  protected List<TestUnit> findTestUnits() {
    return Pitest.findTestUnitsForAllSuppliedClasses(this.pitConfig,
        Collections.<TestDiscoveryListener> emptyList(), this.test);
  }

  public MutationConfig getMutationConfig() {
    return this.config;
  }

}