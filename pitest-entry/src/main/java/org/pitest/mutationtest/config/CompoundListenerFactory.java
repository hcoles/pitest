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

import org.pitest.mutationtest.ListenerArguments;
import org.pitest.mutationtest.MutationResultListener;
import org.pitest.mutationtest.MutationResultListenerFactory;
import org.pitest.plugin.Feature;
import org.pitest.plugin.FeatureSelector;
import org.pitest.plugin.FeatureSetting;

import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CompoundListenerFactory implements MutationResultListenerFactory {

  private final FeatureSelector<MutationResultListenerFactory> features;

  public CompoundListenerFactory(List<FeatureSetting> features, final Collection<MutationResultListenerFactory> children) {
    this.features = new FeatureSelector<>(features, children);
  }

  @Override
  public MutationResultListener getListener(final Properties props,
      final ListenerArguments args) {
    final List<MutationResultListener> listeners = this.features.getActiveFeatures().stream()
            .map(toListener(props, args))
            .collect(Collectors.toList());

    return new CompoundTestListener(listeners);
  }

  @Override
  public String name() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String description() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Feature provides() {
    throw new UnsupportedOperationException();
  }

  private Function<MutationResultListenerFactory, MutationResultListener> toListener(Properties props,
                                                                                     ListenerArguments args) {
    return a -> a.getListener(props, args.withSetting(features.getSettingForFeature(a.provides().name())));
  }
}
