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

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

import org.pitest.functional.F2;
import org.pitest.functional.FCollection;

public final class PitError extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public PitError(final String message, final Throwable cause) {
    super(message + info(), cause);
  }

  public PitError(final String message) {
    super(message + info());
  }

  private static String info() {
    final RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();

    return "\n\nPlease copy and paste the information and the complete stacktrace below when reporting an issue\n"
    + "VM : "
    + rt.getVmName()
    + "\n"
    + "Vendor : "
    + rt.getVmVendor()
    + "\n"
    + "Version : "
    + rt.getVmVersion()
    + "\n"
    + "Uptime : "
    + rt.getUptime()
    + "\n"
    + "Input -> "
    + createInputString(rt.getInputArguments())
    + "\n"
    + "BootClassPathSupported : " + rt.isBootClassPathSupported() + "\n";

  }

  private static String createInputString(final List<String> inputArguments) {
    final StringBuilder sb = new StringBuilder();
    FCollection.fold(append(), sb, inputArguments);
    return sb.toString();
  }

  private static F2<StringBuilder, String, StringBuilder> append() {
    return new F2<StringBuilder, String, StringBuilder>() {
      private int position = 0;

      @Override
      public StringBuilder apply(final StringBuilder a, final String b) {
        this.position++;
        return a.append("\n " + this.position + " : " + b);
      }

    };
  }

}
