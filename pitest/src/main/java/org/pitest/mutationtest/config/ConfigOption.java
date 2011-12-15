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

public enum ConfigOption {

  REPORT_DIR("reportDir"), //
  TARGET_CLASSES("targetClasses"), //
  IN_SCOPE_CLASSES("inScopeClasses"), //
  SOURCE_DIR("sourceDirs"), //
  MUTATIONS("mutators"), //
  DEPENDENCY_DISTANCE("dependencyDistance"), //
  CHILD_JVM("jvmArgs"), //
  MUTATE_STATIC_INITIALIZERS("mutateStaticInits"), //
  THREADS("threads"), //
  INCLUDE_JAR_FILES("includeJarFiles"), //
  TIMEOUT_FACTOR("timeoutFactor"), //
  TIMEOUT_CONST("timeoutConst"), //
  TEST_FILTER("targetTests"), //
  AVOID_CALLS("avoidCallsTo"), //
  EXCLUDED_METHOD("excludedMethods"), //
  MAX_MUTATIONS_PER_CLASS("maxMutationsPerClass"), //
  VERBOSE("verbose"), //
  EXCLUDED_CLASSES("excludedClasses"), //
  OUTPUT_FORMATS("outputFormats"), //
  PROJECT_FILE("project"), //
  CLASSPATH("classPath");

  private final String text;

  ConfigOption(String text) {
    this.text = text;
  }

  public String getParamName() {
    return this.text;
  }

  @Override
  public String toString() {
    return this.text;
  }

}
