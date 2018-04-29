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
package org.pitest.mutationtest.commandline;

import java.util.Optional;
import org.pitest.mutationtest.config.ReportOptions;

public class ParseResult {

  private final ReportOptions  options;
  private final Optional<String> errorMessage;

  public ParseResult(final ReportOptions options, final String errorMessage) {
    this.options = options;
    this.errorMessage = Optional.ofNullable(errorMessage);
  }

  public boolean isOk() {
    return !this.errorMessage.isPresent();
  }

  public ReportOptions getOptions() {
    return this.options;
  }

  public Optional<String> getErrorMessage() {
    return this.errorMessage;
  }

}
