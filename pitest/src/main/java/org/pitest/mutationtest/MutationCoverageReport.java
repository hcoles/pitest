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

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.pitest.ExtendedTestResult;
import org.pitest.TestResult;
import org.pitest.extension.TestListener;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.SideEffect1;
import org.pitest.internal.ClassPath;
import org.pitest.mutationtest.instrument.JavaAgentJarFinder;
import org.pitest.mutationtest.instrument.UnRunnableMutationTestMetaData;
import org.pitest.util.JavaAgent;
import org.pitest.util.TestInfo;
import org.pitest.util.Unchecked;

public abstract class MutationCoverageReport implements Runnable {

  protected final ReportOptions   data;
  protected final ListenerFactory listenerFactory;
  protected final JavaAgent       javaAgentFinder;
  protected final boolean         nonLocalClassPath;

  public MutationCoverageReport(final ReportOptions data,
      final JavaAgent javaAgentFinder, final ListenerFactory listenerFactory,
      final boolean nonLocalClassPath) {
    this.javaAgentFinder = javaAgentFinder;
    this.nonLocalClassPath = nonLocalClassPath;
    this.listenerFactory = listenerFactory;
    this.data = data;
  }

  public abstract void runReport() throws IOException;

  public final void run() {
    try {
      this.runReport();

    } catch (final IOException ex) {
      throw Unchecked.translateCheckedException(ex);
    }
  }

  public static void main(final String args[]) {

    final OptionsParser parser = new OptionsParser();
    final ReportOptions data = parser.parse(args);

    setClassesInScopeToEqualTargetClassesIfNoValueSupplied(data);

    if (data.shouldShowHelp() || !data.isValid()) {
      parser.printHelp();
    } else {
      final MutationCoverageReport instance = selectRunType(data);

      instance.run();

    }

  }

  private static void setClassesInScopeToEqualTargetClassesIfNoValueSupplied(
      final ReportOptions data) {
    if (!data.hasValueForClassesInScope()) {
      data.setClassesInScope(data.getTargetClasses());
    }
  }

  private static MutationCoverageReport selectRunType(final ReportOptions data) {
    return new CodeCentricReport(data, new JavaAgentJarFinder(),
        new HtmlReportFactory(), false);

  }

  protected void reportFailureForClassesWithoutTests(
      final Collection<String> classesWithOutATest,
      final TestListener mutationReportListener) {
    final SideEffect1<String> reportFailure = new SideEffect1<String>() {
      public void apply(final String a) {
        final TestResult tr = new ExtendedTestResult(null, null,
            new UnRunnableMutationTestMetaData("Could not find any tests for "
                + a));
        mutationReportListener.onTestFailure(tr);
      }

    };
    FCollection.forEach(classesWithOutATest, reportFailure);
  }

  protected List<Class<?>> extractTesteesFromTests(
      final Collection<Class<?>> tests) {
    final F<Class<?>, Iterable<Class<?>>> f = new F<Class<?>, Iterable<Class<?>>>() {
      public Iterable<Class<?>> apply(final Class<?> test) {
        return TestInfo.determineTestee(test);
      }
    };
    return FCollection.flatMap(tests, f);
  }

  protected ClassPath getClassPath() {
    return this.data.getClassPath(this.nonLocalClassPath).getOrElse(
        new ClassPath());
  }

}
