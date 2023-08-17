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

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.pitest.functional.Streams.asStream;

public class Glob implements Predicate<String> {

  private final Pattern regex;

  private static final String ZERO_OR_MORE_PACKAGES      = "(?:.*\\.)*";
  private static final String DOUBLE_STAR_PACKAGE_MARKER = "#%#%#";

  public Glob(final String glob) {
    String rectifiedGlob;
    if (glob.startsWith("~")) {
      rectifiedGlob = glob.substring(1);
    } else {
      rectifiedGlob = convertGlobToRegex(glob);
    }
    rectifiedGlob = rectifiedGlob.replace("+", "\\+");
    this.regex = Pattern.compile(rectifiedGlob);
  }

  public boolean matches(final CharSequence seq) {
    return this.regex.matcher(seq).matches();
  }

  public static Function<String, Predicate<String>> toGlobPredicate() {
    return Glob::new;
  }

  public static Collection<Predicate<String>> toGlobPredicates(Collection<String> globs) {
    return asStream(globs)
            .map(Glob.toGlobPredicate())
            .collect(Collectors.toList());
  }

  private static String convertGlobToRegex(final String glob) {
    final String preparedGlob = glob.replace("**.", DOUBLE_STAR_PACKAGE_MARKER);
    final StringBuilder out = new StringBuilder("^");
    for (int i = 0; i < preparedGlob.length(); ++i) {
      final char c = preparedGlob.charAt(i);
      switch (c) {
      case '$':
        out.append("\\$");
        break;
      case '*':
        out.append(".*");
        break;
      case '?':
        out.append('.');
        break;
      case '.':
        out.append("\\.");
        break;
      case '\\':
        out.append("\\\\");
        break;
      default:
        out.append(c);
      }
    }
    out.append('$');
    return out.toString()
        .replace(DOUBLE_STAR_PACKAGE_MARKER, ZERO_OR_MORE_PACKAGES);
  }

  @Override
  public boolean test(final String value) {
    return matches(value);
  }

  @Override
  public String toString() {
    return this.regex.pattern();
  }

}
