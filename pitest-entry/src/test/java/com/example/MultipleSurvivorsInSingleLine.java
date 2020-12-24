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

package com.example;

public class MultipleSurvivorsInSingleLine {

  // generated code, e.g. from by-delegation in Kotlin can have multiple methods in a single line

  // multiple covered methods in a single loc with 1 killed => treated as generated => should not be counted
  // @formatter:off
  public int coveredSurvivor1a() { return 1; } public int coveredSurvivor1b() { return 1; } public int killed1() { return 1; }
  // @formatter:on

  // multiple uncovered methods in a single loc => treated as generated => should not be counted
  // @formatter:off
  public int uncoveredSurvivor2a() { return 2; } public int uncoveredSurvivor2b() { return 2; }
  // @formatter:on

  // a single method in same loc, covered but not asserted => should count as survivor
  public int coveredSurvivor3() {
    return 3;
  }

  // a single method in same loc, not covered => should count as uncovered
  public int uncoveredSurvivor4() {
    return 4;
  }

  // a single method in same loc, covered and asserted => should count killed
  public int killed5() {
    return 5;
  }
}
