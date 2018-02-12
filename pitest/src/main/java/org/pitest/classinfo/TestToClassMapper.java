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
package org.pitest.classinfo;

import java.util.Optional;

public class TestToClassMapper {

  private static final int TEST_LENGTH = "Test".length();
  private final Repository repository;

  public TestToClassMapper(final Repository repository) {
    this.repository = repository;
  }

  public Optional<ClassName> findTestee(final String className) {

    final ClassName name = ClassName.fromString(className);

    if (name.asJavaName().endsWith("Test")
        && tryName(name.withoutSuffixChars(TEST_LENGTH))) {
      return Optional.ofNullable(name.withoutSuffixChars(TEST_LENGTH));
    }

    if (name.getNameWithoutPackage().asJavaName().startsWith("Test")
        && tryName(name.withoutPrefixChars(TEST_LENGTH))) {
      return Optional.ofNullable(name.withoutPrefixChars(TEST_LENGTH));
    }

    return Optional.empty();
  }

  private boolean tryName(final ClassName name) {
    return this.repository.hasClass(name);
  }

}
