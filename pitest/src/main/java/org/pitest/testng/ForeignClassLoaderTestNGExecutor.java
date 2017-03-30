package org.pitest.testng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.testng.TestNG;
import org.testng.xml.XmlSuite;

public class ForeignClassLoaderTestNGExecutor implements Callable<List<String>> {

  private final XmlSuite suite;

  public ForeignClassLoaderTestNGExecutor(XmlSuite suite) {
    this.suite = suite;
  }

  @Override
  public List<String> call() throws Exception {
    List<String> queue = new ArrayList<String>();
    final ForeignClassLoaderAdaptingListener listener = new ForeignClassLoaderAdaptingListener(queue);
    final TestNG testng = new TestNG(false);
    testng.setDefaultSuiteName(this.suite.getName());
    testng.setXmlSuites(Collections.singletonList(this.suite));

    testng.addListener(listener);
    testng.addInvokedMethodListener(new FailFast(listener));
    testng.run();

    return queue;
  }

}
