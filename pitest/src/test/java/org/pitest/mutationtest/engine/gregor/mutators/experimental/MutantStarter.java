/*
 * Copyright 2011 Henry Coles and Stefan Penndorf
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
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pitest.mutationtest.engine.gregor.mutators.experimental;

import java.util.concurrent.Callable;

/**
 * <p>
 * "Starter" Class for Mutants. Mutator (System) Tests are designed to mutate
 * method calls and check them. The construction of the "mutee" will be done
 * using the original, unmutated code. This <code>MutantStarter</code> will be
 * used as a wrapper for the mutee and will construct the mutee in it's own
 * <code>call()</code> method thus executing the mutated version of the mutees
 * constructor.
 * </p>
 * <p>
 * You can subclass this starter and overwrite the <code>constructMutee()</code>
 * method in order to use your own construction logic.
 * </p>
 *
 *
 * @author Stefan Penndorf <stefan.penndorf@gmail.com>
 */
public class MutantStarter<T> implements Callable<T> {

  private final Class<? extends Callable<T>> mutantCallableClass;

  protected MutantStarter() {
    this(null);
  }

  /**
   * Creates a new mutant starter using the default no-arg constructor of the
   * class supplied. The no-arg constructor and the class itself must have at
   * least package visibility.
   *
   * @param mutantCallableClass
   *          class of mutant callable to create and use in <code>call()</code>.
   */
  public MutantStarter(final Class<? extends Callable<T>> mutantCallableClass) {
    super();
    this.mutantCallableClass = mutantCallableClass;
  }

  @Override
  public T call() throws Exception {
    return constructMutee().call();
  }

  protected Callable<T> constructMutee() throws Exception {
    return this.mutantCallableClass.newInstance();
  }

}
