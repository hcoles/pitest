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

package org.pitest.mutationtest.instrument;

import static org.pitest.util.Unchecked.translateCheckedException;

import java.util.List;

import org.pitest.DefaultStaticConfig;
import org.pitest.Pitest;
import org.pitest.extension.Container;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.EmptyConfiguration;
import org.pitest.functional.F2;
import org.pitest.mutationtest.CheckTestHasFailedResultListener;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.instrument.ResultsReader.DetectionStatus;

public abstract class AbstractWorker {

  protected final Mutater                       mutater;
  protected final ClassLoader                   loader;
  protected final F2<Class<?>, byte[], Boolean> hotswap;

  public AbstractWorker(final F2<Class<?>, byte[], Boolean> hotswap,
      final Mutater mutater, final ClassLoader loader) {
    this.loader = loader;
    this.mutater = mutater;
    this.hotswap = hotswap;
  }

  protected DetectionStatus doTestsDetectMutation(final Container c,
      final List<TestUnit> tests) {
    try {
      final CheckTestHasFailedResultListener listener = new CheckTestHasFailedResultListener();

      final EmptyConfiguration conf = new EmptyConfiguration();

      final DefaultStaticConfig staticConfig = new DefaultStaticConfig();
      staticConfig.addTestListener(listener);

      final Pitest pit = new Pitest(staticConfig, conf);
      pit.run(c, tests);

      return listener.status();
    } catch (final Exception ex) {
      throw translateCheckedException(ex);
    }

  }

}
