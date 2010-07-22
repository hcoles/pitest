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
package org.pitest.mutationtest.loopbreak;

import static org.pitest.util.Unchecked.translateCheckedException;

import java.lang.reflect.Method;

import org.pitest.extension.ResultCollector;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.TestUnitDecorator;
import org.pitest.functional.predicate.Predicate;
import org.pitest.internal.IsolationUtils;

public class LoopBreakDecorator extends TestUnitDecorator {

  private final long maxEndTime;

  public LoopBreakDecorator(final TestUnit child, final long maxEndTime) {
    super(child);
    this.maxEndTime = maxEndTime;
  }

  public void execute(final ClassLoader loader, final ResultCollector rc) {
    // StaticTimerThatLimitsUsToASingleThread.setMaxEndTime(maxEndTime);

    final Class<?> c = IsolationUtils.convertForClassLoader(loader,
        PerContainerTimelimitCheck.class);
    final Predicate<Method> p = new Predicate<Method>() {

      public Boolean apply(final Method a) {
        return a.getName().equals("setMaxEndTime");
      }

    };
    final Method m = org.pitest.reflection.Reflection.publicMethod(c, p);
    final Object[] params = { this.maxEndTime };
    try {
      m.invoke(null, params);
    } catch (final Exception e) {
      throw translateCheckedException(e);
    }

    this.child().execute(loader, rc);

  }

}
