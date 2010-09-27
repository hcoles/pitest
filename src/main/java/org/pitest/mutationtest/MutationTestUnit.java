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

import static org.pitest.util.Unchecked.translateCheckedException;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.util.ClassLoaderRepository;
import org.pitest.Description;
import org.pitest.Pitest;
import org.pitest.extension.Configuration;
import org.pitest.extension.ResultCollector;
import org.pitest.extension.TestUnit;
import org.pitest.functional.SideEffect1;
import org.pitest.internal.ClassPath;
import org.pitest.internal.IsolationUtils;
import org.pitest.testunit.AbstractTestUnit;
import org.pitest.util.HotSwap;
import org.pitest.util.JavaProcess;

import com.reeltwo.jumble.mutation.Mutater;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.VMStartException;
import com.thoughtworks.xstream.XStream;

public class MutationTestUnit extends AbstractTestUnit {

  private static final Logger  logger = Logger.getLogger(MutationTestUnit.class
                                          .getName());

  private final Class<?>       test;
  private final Class<?>       classToMutate;

  private final MutationConfig config;
  private final Configuration  pitConfig;

  // FIXME should the interface not take a list of testunits to run rather
  // than a single test class?
  public MutationTestUnit(final Class<?> test, final Class<?> classToMutate,
      final MutationConfig mutationConfig, final Configuration pitConfig,
      final Description description) {
    super(description, null);
    this.classToMutate = classToMutate;
    this.test = test;
    this.config = mutationConfig;
    this.pitConfig = pitConfig;
  }

  private List<TestUnit> findTestUnits() {
    return Pitest.findTestUnitsForAllSuppliedClasses(this.pitConfig, this.test);
  }

  @Override
  public void execute(final ClassLoader loader, final ResultCollector rc) {
    try {
      rc.notifyStart(this.description());
      runTests(rc, loader);
    } catch (final Throwable ex) {
      rc.notifyEnd(this.description(), ex);
    }

  }


  private void runTests(final ResultCollector rc, final ClassLoader loader) {
    final MutationTestUnitWorker worker = new MutationTestUnitWorker(rc, this.findTestUnits(), classToMutate, config, description());
    worker.runTests(loader);
  }

  
  
  public MutationConfig getMutationConfig() {
    return this.config;
  }



}
