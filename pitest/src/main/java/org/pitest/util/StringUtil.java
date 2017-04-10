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

public class StringUtil {

  public static String join(final Iterable<String> strings,
      final String separator) {
    final StringBuilder sb = new StringBuilder();
    String sep = "";
    for (final String s : strings) {
      sb.append(sep).append(s);
      sep = separator;
    }
    return sb.toString();
  }

  public static String newLine() {
    return System.getProperty("line.separator");
  }

  public static String separatorLine(final char c) {
    return repeat(c, 80);
  }

  public static String separatorLine() {
    return repeat('-', 80);
  }

  public static String repeat(final char c, final int n) {
    return new String(new char[n]).replace('\0', c);
  }

  public static String escapeBasicHtmlChars(final String s) {
    final StringBuilder sb = new StringBuilder();
    escapeBasicHtmlChars(s, sb);
    return sb.toString();
  }

  public static void escapeBasicHtmlChars(final String s,
      final StringBuilder out) {

    for (int i = 0; i < s.length(); i++) {
      final char c = s.charAt(i);
      final int v = c;
      if ((v < 32) || (v > 127) || (v == 38) || (v == 39) || (v == 60)
          || (v == 62) || (v == 34)) {
        out.append('&');
        out.append('#');
        out.append(v);
        out.append(';');
      } else {
        out.append(c);
      }
    }
  }

  public static boolean isNullOrEmpty(final String s) {
    return s == null || s.isEmpty();
  }

}
