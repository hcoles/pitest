package org.pitest.coverage.execute;

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

import java.util.Properties;

import org.pitest.functional.predicate.Predicate;

public class SlaveArguments {

  private final Properties        systemProperties;
  private final Predicate<String> filter;
  private final int               port;
  private final boolean           verbose;

  public SlaveArguments(final Properties systemProperties,
      final Predicate<String> filter, final int port, final boolean verbose) {
    this.systemProperties = systemProperties;
    this.filter = filter;
    this.port = port;
    this.verbose = verbose;
  }

  public Properties getSystemProperties() {
    return this.systemProperties;
  }

  public Predicate<String> getFilter() {
    return this.filter;
  }

  public int getPort() {
    return this.port;
  }

  public boolean isVerbose() {
    return this.verbose;
  }

}
