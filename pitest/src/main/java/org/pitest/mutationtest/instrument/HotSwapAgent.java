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

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public class HotSwapAgent {

  private static Instrumentation instrumentation;

  public static void premain(final String agentArguments,
      final Instrumentation inst) {
    System.out.println("Installing PIT agent");
    instrumentation = inst;
  }

  public static boolean hotSwap(final Class<?> mutateMe, final byte[] bytes) {

    final ClassDefinition[] definitions = { new ClassDefinition(mutateMe, bytes) };

    try {
      instrumentation.redefineClasses(definitions);

      return true;
    } catch (final ClassNotFoundException e) {
      // swallow
    } catch (final UnmodifiableClassException e) {
      // swallow
    } catch (final java.lang.VerifyError e) {
      // swallow
    }
    return false;
  }

}
