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
package org.pitest.extension.common;

import static org.pitest.util.Unchecked.translateCheckedException;

import org.pitest.DefaultStaticConfig;
import org.pitest.annotations.StaticConfigurationClass;
import org.pitest.extension.StaticConfigUpdater;
import org.pitest.extension.StaticConfiguration;

public class DefaultStaticConfigUpdater implements StaticConfigUpdater {

  public StaticConfiguration apply(final StaticConfiguration current,
      final Class<?> clazz) {
    try {

      final StaticConfigurationClass annotation = clazz
          .getAnnotation(StaticConfigurationClass.class);
      final DefaultStaticConfig conf = new DefaultStaticConfig(current);
      if (annotation != null) {
        for (final Class<? extends StaticConfiguration> c : annotation.value()) {
          final StaticConfiguration inst = c.newInstance();
          conf.addConfiguration(inst);
        }
      }

      return conf;

    } catch (final Exception ex) {
      throw translateCheckedException(ex);
    }
  }

}
