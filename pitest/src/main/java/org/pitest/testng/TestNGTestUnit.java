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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import java.util.List;
import org.pitest.testapi.AbstractTestUnit;
import org.pitest.testapi.ResultCollector;
import org.pitest.testapi.TestGroupConfig;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * Runs tests within a class via TestNG. It would be possible to create a test
 * unit per method using TestNG's filters but this is about ten times slower and
 * probably more than negates any advantage from more finely targeting the
 * tests.
 */
public class TestNGTestUnit extends AbstractTestUnit {

  // needs to be static as jmockit assumes only a single instance per jvm
  private static final TestNG                TESTNG = new TestNG(false);
  private static final MutableTestListenerWrapper LISTENER = new MutableTestListenerWrapper();

  static {
    TESTNG.addListener(LISTENER);
    TESTNG.addInvokedMethodListener(new FailFast(LISTENER));
  }
  
  private final Class<?>                     clazz;
  private final TestGroupConfig              config;
  private final Collection<String> includedTestMethods;



  public TestNGTestUnit(
          final Class<?> clazz, final TestGroupConfig config, Collection<String> includedTestMethods) {
    super(new org.pitest.testapi.Description("_", clazz));
    this.clazz = clazz;
    this.config = config;
    this.includedTestMethods = includedTestMethods;
  }

  @Override
  public void execute(final ResultCollector rc) {
    synchronized (TESTNG) {
        executeInCurrentLoader(rc);
    }
  }



  private void executeInCurrentLoader(final ResultCollector rc) {
    final TestNGAdapter listener = new TestNGAdapter(this.clazz,
        this.getDescription(), rc);

    final XmlSuite suite = createSuite();

    TESTNG.setDefaultSuiteName(suite.getName());
    TESTNG.setXmlSuites(Collections.singletonList(suite));

    LISTENER.setChild(listener);
    try {
      TESTNG.run();
    } finally {
      // yes this is hideous
      LISTENER.setChild(null);
    }
  }

  private XmlSuite createSuite() {
    final XmlSuite suite = new XmlSuite();
    suite.setName(this.clazz.getName());
    suite.setSkipFailedInvocationCounts(true);
    final XmlTest test = new XmlTest(suite);
    test.setName(this.clazz.getName());
    final XmlClass xclass = new XmlClass(this.clazz.getName());
    test.setXmlClasses(Collections.singletonList(xclass));

    if (!this.includedTestMethods.isEmpty()) {
      List<XmlInclude> xmlIncludedTestMethods = new ArrayList<>();
      for (String includedTestMethod : includedTestMethods) {
        XmlInclude includedMethod = new XmlInclude(includedTestMethod);
        xmlIncludedTestMethods.add(includedMethod);
      }
      xclass.setIncludedMethods(xmlIncludedTestMethods);
    }

    if (!this.config.getExcludedGroups().isEmpty()) {
      suite.setExcludedGroups(this.config.getExcludedGroups());
    }

    if (!this.config.getIncludedGroups().isEmpty()) {
      suite.setIncludedGroups(this.config.getIncludedGroups());
    }

    return suite;
  }

}

class FailFast implements IInvokedMethodListener {
  
  private final FailureTracker listener;
  
  FailFast(FailureTracker listener) {
    this.listener = listener;
  }

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    if (listener.hasHadFailure()) {
      throw new SkipException("Skipping");
    }
  }

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
 
  }
  
}

class MutableTestListenerWrapper implements ITestListener, FailureTracker {
  private TestNGAdapter child;
  
  public void setChild(TestNGAdapter child) {
    this.child = child;
  }

  public boolean hasHadFailure() {
    return child.hasHadFailure();
  }

  public void onTestStart(ITestResult result) {
    child.onTestStart(result);
  }

  public void onTestSuccess(ITestResult result) {
    child.onTestSuccess(result);
  }

  public void onTestFailure(ITestResult result) {
    child.onTestFailure(result);
  }

  public void onTestSkipped(ITestResult result) {
    child.onTestSkipped(result);
  }

  public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    child.onTestFailedButWithinSuccessPercentage(result);
  }

  public void onStart(ITestContext context) {
    child.onStart(context);
  }

  public void onFinish(ITestContext context) {
    child.onFinish(context);
  }
}
