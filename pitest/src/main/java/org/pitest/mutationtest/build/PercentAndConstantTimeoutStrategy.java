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

package org.pitest.mutationtest.build;

import org.pitest.mutationtest.TimeoutLengthStrategy;

public class PercentAndConstantTimeoutStrategy implements TimeoutLengthStrategy {

  private static final long serialVersionUID = 1L;

  public static final float DEFAULT_FACTOR   = 1.25f;
  public static final long  DEFAULT_CONSTANT = 4000;

  private final float       percent;
  private final long        constant;

  public PercentAndConstantTimeoutStrategy(final float percent,
      final long constant) {
    this.percent = percent;
    this.constant = constant;
  }

  @Override
  public long getAllowedTime(final long normalDuration) {
    return Math.round(normalDuration * this.percent) + this.constant;
  }

}
