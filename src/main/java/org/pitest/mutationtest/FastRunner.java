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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;

import org.pitest.DefaultStaticConfig;
import org.pitest.Pitest;
import org.pitest.containers.UnContainer;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.EmptyConfiguration;
import org.pitest.internal.ClassPath;
import org.pitest.internal.IsolationUtils;
import org.pitest.internal.classloader.DefaultPITClassloader;

import com.thoughtworks.xstream.XStream;

public class FastRunner {

  // public static long cycleTime;

  public static void main(final String[] args) {

    final PrintStream outStream = System.out;
    final BufferedReader stdIn = new BufferedReader(new InputStreamReader(
        System.in));

    try {
      final DefaultPITClassloader cl = createClassLoader(stdIn);
      final List<TestUnit> tests = getTestList(stdIn, cl);

      final int mutationCount = Integer.parseInt(args[0]);

      // vmBreak();

      pauseBeforeUnmutatedTest();

      runTestsWithCurrentTesteeImplementation(outStream, tests);
      System.err.println("Ran unmutated tests ");

      for (int i = 0; i != mutationCount; i++) {
        System.err.println("Loop iteration " + i);
        pauseBeforeMutatedTestRun();
        runTestsWithCurrentTesteeImplementation(outStream, tests);
      }
    } catch (final IOException ex) {
      ex.printStackTrace();
    }

  }

  private static List<TestUnit> getTestList(final BufferedReader stdIn,
      final DefaultPITClassloader cl) throws IOException {
    final String xml = stdIn.readLine();

    System.err.println("Got xml " + xml);

    final List<TestUnit> tests = xmlToTestGroup(xml, cl);
    return tests;
  }

  private static DefaultPITClassloader createClassLoader(
      final BufferedReader stdIn) throws IOException {
    final String classPathXML = stdIn.readLine();
    final XStream xstream = new XStream();
    final ClassPath cp = (ClassPath) xstream.fromXML(classPathXML);

    final DefaultPITClassloader cl = new DefaultPITClassloader(cp,
        IsolationUtils.getContextClassLoader());
    return cl;
  }

  private static void runTestsWithCurrentTesteeImplementation(
      final PrintStream outStream, final List<TestUnit> tests) {

    final CheckTestHasFailedResultListener listener = new CheckTestHasFailedResultListener();

    final EmptyConfiguration conf = new EmptyConfiguration();
    final Pitest pit = new Pitest(conf);
    final DefaultStaticConfig staticConfig = new DefaultStaticConfig();
    staticConfig.addTestListener(listener);
    pit.run(new UnContainer(), staticConfig, tests);

    // report
    System.err.println("(child) Reporting  "
        + listener.resultIndicatesSuccess());
    outStream.println(listener.resultIndicatesSuccess());

  }

  @SuppressWarnings("unchecked")
  private static List<TestUnit> xmlToTestGroup(final String xml,
      final ClassLoader cl) {
    try {
      final XStream xstream = new XStream();
      xstream.setClassLoader(cl);
      return (List<TestUnit>) xstream.fromXML(xml);

    } catch (final Exception ex) {
      throw translateCheckedException(ex);
    }
  }

  private static void pauseBeforeMutatedTestRun() {
    // DO NOT REMOVE OR RENAME THIS METHOD - a breakpoint
    // is set on it by the controlling process
  }

  private static void pauseBeforeUnmutatedTest() {
    // DO NOT REMOVE OR RENAME THIS METHOD - a breakpoint
    // is set on it by the controlling process
  }

}
