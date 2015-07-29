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
package org.pitest.util;

public class TimeSpan {

  private long start;
  private long end;

  public TimeSpan(final long start, final long end) {
    this.start = start;
    this.end = end;
  }

  public long duration() {
    return this.end - this.start;
  }

  public long getStart() {
    return this.start;
  }

  public long getEnd() {
    return this.end;
  }

  public void setStart(final long start) {
    this.start = start;
  }

  public void setEnd(final long end) {
    this.end = end;
  }

  @Override
  public String toString() {
    final long millis = duration();
    final int seconds = (int) (millis / 1000) % 60;
    final int minutes = (int) ((millis / (1000 * 60)) % 60);
    final int hours = (int) ((millis / (1000 * 60 * 60)) % 24);

    if (hours != 0) {
      return "" + hours + " hours, " + minutes + " minutes and " + seconds
          + " seconds";
    }

    if (minutes != 0) {
      return "" + minutes + " minutes and " + seconds + " seconds";
    }

    if (seconds != 0) {
      return "" + seconds + " seconds";
    }

    return "< 1 second";

  }

}
