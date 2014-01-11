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
package org.pitest.testng;

import java.util.Collections;
import java.util.List;

import org.pitest.testapi.TestGroupConfig;
import org.pitest.testapi.AbstractTestUnit;
import org.pitest.testapi.ResultCollector;
import org.pitest.testapi.foreignclassloader.Events;
import org.pitest.util.ClassLoaderDetectionStrategy;
import org.pitest.util.IsolationUtils;
import org.pitest.util.Unchecked;
import org.testng.ITestListener;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * Runs tests within a class via TestNG. It would be possible to create a test
 * unit per method using TestNG's filters but this is about ten times slower and
 * probably more than negates any advantage from more finely targeting the
 * tests.
 */
public class TestNGTestUnit extends AbstractTestUnit {

  private final ClassLoaderDetectionStrategy classloaderDetection;
  private final Class<?>                     clazz;
  private final TestGroupConfig config;

  public TestNGTestUnit(
      final ClassLoaderDetectionStrategy classloaderDetection,
      final Class<?> clazz, final TestGroupConfig config) {
    super(new org.pitest.testapi.Description("_", clazz));
    this.clazz = clazz;
    this.classloaderDetection = classloaderDetection;
    this.config = config;
  }

  public TestNGTestUnit(final Class<?> clazz, final TestGroupConfig config) {
    this(IsolationUtils.loaderDetectionStrategy(), clazz, config);
  }

  @Override
  public void execute(final ClassLoader loader, final ResultCollector rc) {

    if (this.classloaderDetection.fromDifferentLoader(this.clazz, loader)) {
      executeInForeignLoader(rc,loader);
    } else {
      executeInCurrentLoader(rc);
    }
  }

  private void executeInForeignLoader(ResultCollector rc, ClassLoader loader) {
    ForeignClassLoaderTestNGExecutor e = new ForeignClassLoaderTestNGExecutor(createSuite());
    try {
      List<String> q = e.call();
      Events.applyEvents(q, rc,
          this.getDescription());
    } catch (Exception ex) {
      throw Unchecked.translateCheckedException(ex);
    }
    
  }

  private void executeInCurrentLoader( final ResultCollector rc) {
    final ITestListener listener = new TestNGAdapter(this.clazz,
        this.getDescription(), rc);
    final TestNG testng = new TestNG(false);

    final XmlSuite suite = createSuite();

    testng.setDefaultSuiteName(suite.getName());
    testng.setXmlSuites(Collections.singletonList(suite));

    testng.addListener(listener);
    testng.run();
  }

  private XmlSuite createSuite() {
    final XmlSuite suite = new XmlSuite();
    suite.setName(this.clazz.getName());
    final XmlTest test = new XmlTest(suite);
    test.setName(this.clazz.getName());
    final XmlClass xclass = new XmlClass(this.clazz.getName());
    test.setXmlClasses(Collections.singletonList(xclass));
    if (!this.config.getExcludedGroups().isEmpty()) {
      suite.setExcludedGroups(this.config.getExcludedGroups());
    }

    if (!this.config.getIncludedGroups().isEmpty()) {
      suite.setIncludedGroups(this.config.getIncludedGroups());
    }

    return suite;
  }

}
