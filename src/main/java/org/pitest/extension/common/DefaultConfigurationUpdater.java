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

import org.pitest.ConcreteConfiguration;
import org.pitest.annotations.ConfigurationClass;
import org.pitest.extension.Configuration;
import org.pitest.extension.ConfigurationUpdater;

public class DefaultConfigurationUpdater implements ConfigurationUpdater {

  public Configuration updateConfiguration(final Class<?> clazz,
      final Configuration current) {

    try {

      final ConfigurationClass annotation = clazz
          .getAnnotation(ConfigurationClass.class);
      final ConcreteConfiguration conf = new ConcreteConfiguration(current);
      if (annotation != null) {
        for (final Class<? extends Configuration> c : annotation.value()) {
          final Configuration inst = c.newInstance();
          conf.addConfiguration(inst);
        }
      }

      return conf;

    } catch (final Exception ex) {
      throw translateCheckedException(ex);
    }
  }

}
