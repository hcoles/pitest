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
package org.pitest.mutationtest.config;

import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.ListenerArguments;
import org.pitest.mutationtest.MutationResultListener;
import org.pitest.mutationtest.MutationResultListenerFactory;

public class CompoundListenerFactory implements MutationResultListenerFactory {

  private final Iterable<MutationResultListenerFactory> children;

  public CompoundListenerFactory(
      final Iterable<MutationResultListenerFactory> children) {
    this.children = children;
  }

  public MutationResultListener getListener(final ListenerArguments args) {
    return new CompoundTestListener(FCollection.map(this.children,
        factoryToListener(args)));
  }

  private F<MutationResultListenerFactory, MutationResultListener> factoryToListener(
      final ListenerArguments args) {
    return new F<MutationResultListenerFactory, MutationResultListener>() {

      public MutationResultListener apply(final MutationResultListenerFactory a) {
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
