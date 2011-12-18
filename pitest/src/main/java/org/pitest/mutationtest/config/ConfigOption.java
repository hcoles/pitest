/*
 * Copyright 2011 Henry Coles
 * 
 * Licensed under the Apache License, Version 2.0 ("the "License""); 
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

import org.pitest.mutationtest.instrument.PercentAndConstantTimeoutStrategy;

public enum ConfigOption {

  REPORT_DIR("reportDir"), //
  TARGET_CLASSES("targetClasses"), //
  IN_SCOPE_CLASSES("inScopeClasses"), //
  SOURCE_DIR("sourceDirs"), //
  MUTATIONS("mutators"), //
  DEPENDENCY_DISTANCE("dependencyDistance", -1), //
  CHILD_JVM("jvmArgs"), //
  MUTATE_STATIC_INITIALIZERS("mutateStaticInits", false), //
  THREADS("threads", 1), //
  INCLUDE_JAR_FILES("includeJarFiles", false), //
  TIMEOUT_FACTOR("timeoutFactor",
      PercentAndConstantTimeoutStrategy.DEFAULT_FACTOR), //
  TIMEOUT_CONST("timeoutConst",
      PercentAndConstantTimeoutStrategy.DEFAULT_CONSTANT), //
  TEST_FILTER("targetTests"), //
  AVOID_CALLS("avoidCallsTo"), //
  EXCLUDED_METHOD("excludedMethods"), //
  MAX_MUTATIONS_PER_CLASS("maxMutationsPerClass", 0), //
  VERBOSE("verbose", false), //
  EXCLUDED_CLASSES("excludedClasses"), //
  OUTPUT_FORMATS("outputFormats"), //
  PROJECT_FILE("project"), //
  CLASSPATH("classPath"), //
  FAIL_WHEN_NOT_MUTATIONS("failWhenNoMutations", true);

  private final String text;
  private final Object defaultValue;

  ConfigOption(String text) {
    this(text, null);
  }

  ConfigOption(String text, Object defaultValue) {
    this.text = text;
    this.defaultValue = defaultValue;
  }

  public String getParamName() {
    return this.text;
  }

  @SuppressWarnings("unchecked")
  public <T> T getDefault(Class<T> type) {
    // so much for type safety
    return (T) this.defaultValue;
  }

  @Override
  public String toString() {
    return this.text;
  }

}
