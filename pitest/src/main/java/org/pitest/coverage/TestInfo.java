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
package org.pitest.coverage;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;

import org.pitest.classinfo.ClassName;
import java.util.Optional;


public final class TestInfo implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String            name;
  private final String            definingClass;

  private final int               time;
  private final int               blocks;

  private final ClassName         testee;

  public TestInfo(final String definingClass, final String name,
      final int time, final Optional<ClassName> testee, final int blocksCovered) {
    this.definingClass = internIfNotNull(definingClass);
    this.name = name;
    this.time = time;
    this.testee = testee.orElse(null);
    this.blocks = blocksCovered;
  }

  public String getName() {
    return this.name;
  }

  public int getTime() {
    return this.time;
  }

  public int getNumberOfBlocksCovered() {
    return this.blocks;
  }

  @Override
  public String toString() {
    return this.name;
  }

  public static Function<TestInfo, String> toName() {
    return TestInfo::getName;
  }

  public static Function<TestInfo, ClassName> toDefiningClassName() {
    return a -> ClassName.fromString(a.definingClass);
  }

  public boolean directlyHits(final ClassName targetClass) {
    return this.testee != null && this.testee.equals(targetClass);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, definingClass);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final TestInfo other = (TestInfo) obj;
    return Objects.equals(name, other.name)
            && Objects.equals(definingClass, other.definingClass);
  }

  private static String internIfNotNull(final String string) {
    if (string == null) {
      return null;
    }
    return string.intern();
  }

}
