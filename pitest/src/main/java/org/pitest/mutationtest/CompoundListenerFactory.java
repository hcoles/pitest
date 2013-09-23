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
package org.pitest.mutationtest;

import org.pitest.functional.F;
import org.pitest.functional.FCollection;

public class CompoundListenerFactory implements ListenerFactory {

  private final Iterable<ListenerFactory> children;

  public CompoundListenerFactory(final Iterable<ListenerFactory> children) {
    this.children = children;
  }

  public MutationResultListener getListener(ListenerArguments args) {
    return new CompoundTestListener(FCollection.map(this.children,
        factoryToListener(args)));
  }

  private F<ListenerFactory, MutationResultListener> factoryToListener(final ListenerArguments args) {
    return new F<ListenerFactory, MutationResultListener>() {

      public MutationResultListener apply(final ListenerFactory a) {
        return a.getListener(args);
      }

    };
  }

  public String name() {
    throw new UnsupportedOperationException();
  }

  public String description() {
    throw new UnsupportedOperationException();
  }

}
