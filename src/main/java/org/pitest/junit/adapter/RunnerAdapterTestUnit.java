///*
// * Copyright 2010 Henry Coles
// * 
// * Licensed under the Apache License, Version 2.0 (the "License"); 
// * you may not use this file except in compliance with the License. 
// * You may obtain a copy of the License at 
// * 
// * http://www.apache.org/licenses/LICENSE-2.0 
// * 
// * Unless required by applicable law or agreed to in writing, 
// * software distributed under the License is distributed on an "AS IS" BASIS, 
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// * See the License for the specific language governing permissions and limitations under the License. 
// */
//package org.pitest.junit.adapter;
//
//import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.io.Serializable;
//import java.util.Collections;
//import java.util.Iterator;
//
//import org.pitest.Description;
//import org.pitest.extension.ResultCollector;
//import org.pitest.extension.TestUnit;
//import org.pitest.junit.CustomRunnerExecutor;
//
//public class RunnerAdapterTestUnit implements TestUnit, Serializable {
//
//  private static final long                      serialVersionUID = 1L;
//
//  private final Description                      description;
//  private transient org.junit.runner.Description junitDescription;
//  private final RunnerAdapter                    runner;
//
//  public RunnerAdapterTestUnit(final RunnerAdapter runner,
//      final org.junit.runner.Description junitDescription,
//      final Description description) {
//    this.description = description;
//    this.runner = runner;
//    this.junitDescription = junitDescription;
//  }
//
//  public void execute(final ClassLoader loader, final ResultCollector rc) {
//    this.runner.execute(loader, this, rc);
//  }
//
//  public org.junit.runner.Description getJunitDescription() {
//    return this.junitDescription;
//  }
//
//  private void readObject(final ObjectInputStream aInputStream)
//      throws ClassNotFoundException, IOException {
//
//    aInputStream.defaultReadObject();
//
//    final String description = (String) aInputStream.readObject();
//
//    this.junitDescription = this.runner
//        .getTestUnitDescriptionForString(description);
//
//  }
//
//  private void writeObject(final ObjectOutputStream aOutputStream)
//      throws IOException {
//
//    aOutputStream.defaultWriteObject();
//    aOutputStream.writeObject(CustomRunnerExecutor
//        .descriptionToString(this.junitDescription));
//  }
//
//  RunnerAdapter getAdapter() {
//    return this.runner;
//  }
//
//  public Description description() {
//    return this.description;
//  }
//
//  public Iterator<TestUnit> iterator() {
//    return Collections.<TestUnit> emptyList().iterator();
//  }
//
// }
