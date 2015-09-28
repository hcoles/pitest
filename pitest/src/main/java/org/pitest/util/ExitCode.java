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
package org.pitest.util;

public enum ExitCode {

  OK(0), OUT_OF_MEMORY(11), UNKNOWN_ERROR(13), TIMEOUT(14), JUNIT_ISSUE(15);

  private final int code;

  ExitCode(final int code) {
    this.code = code;
  }

  public int getCode() {
    return this.code;
  }

  public static ExitCode fromCode(final int code) {
    for (final ExitCode each : values()) {
      if (each.getCode() == code) {
        return each;
      }
    }

    return UNKNOWN_ERROR;
  }

  public boolean isOk() {
    return this.equals(OK);
  }

}
