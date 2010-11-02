/*
 * Based on http://code.google.com/p/javacoveragent/ by
 * "alex.mq0" and "dmitry.kandalov"
 * 
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

package org.pitest.coverage.codeassist;

import java.lang.instrument.Instrumentation;

import org.pitest.coverage.calculator.CodeCoverageStore;
import org.pitest.coverage.calculator.InvokeQueue;
import org.pitest.coverage.calculator.InvokeQueueCleanerManager;
import org.pitest.coverage.calculator.InvokeStatistics;

/**
 * @author ivanalx
 * @date 26.01.2009 15:09:11
 */
public class CoverageAgent {
  private static final int CLEANER_AMOUNT = 10;

  public static void premain(final String agentArgs,
      final Instrumentation instrument) {
    // final Properties properties = loadProperties(agentArgs);
    // final Configuration configuration = new Configuration(properties);

    // System.err.println("WARNING! Code coverage is enabled");
    // if (configuration.isCoverageLines()) {
    // System.err.println("Coverage enabled for lines.");
    // }
    // if (configuration.isCoverageMethods()) {
    // System.err.println("Coverage enabled for methods.");
    // }

    final InvokeStatistics invokeStatistics = new InvokeStatistics();
    final InvokeQueue invokeQueue = new InvokeQueue();
    CodeCoverageStore.init(invokeQueue, invokeStatistics);
    InvokeQueueCleanerManager.start(invokeQueue, invokeStatistics,
        CLEANER_AMOUNT);

    // final ClassTransformer transformer = new ClassTransformer();
    // transformer.setCoverageLine(configuration.isCoverageLines());
    // transformer.setCoverageMethod(configuration.isCoverageMethods());
    // transformer.setIncludePrefix(configuration.getIncludePrefix());
    // transformer.setExcludePrefix(configuration.getExcludePrefix());
    // instrument.addTransformer(transformer);

  }

  // private static Properties loadProperties(final String agentArgs) {
  // final Properties properties = new Properties();
  // try {
  // if ((agentArgs != null) && (agentArgs.trim().length() != 0)) {
  // final File propertiesFile = new File(agentArgs);
  // if (!propertiesFile.exists()) {
  // throw new RuntimeException(
  // "Code coverage agent can't load property file from " + agentArgs
  // + ". File not exists.");
  // }
  // try {
  // properties.load(new FileInputStream(propertiesFile));
  // } catch (final IOException e) {
  // throw new RuntimeException(e);
  // }
  // }
  // } catch (final Exception e) { // TODO why catch Exception ?
  // throw new RuntimeException(e);
  // }
  // return properties;
  // }

}
