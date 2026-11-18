package org.pitest.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.pitest.functional.Streams.asStream;

/**
 * Cheap and cheerful parser for simple feature config language.
 *
 * Syntax is designed to require no escaping when embedding in XML
 * document such as a maven pom.
 *
 * Parameters may be separated by whitespace, by commas, or by both.
 *
 * A value can be enclosed in double quotes. A quoted value is taken literally,
 * so it may contain the characters that would otherwise end the value or
 * separate parameters, for example {@code +FOO(file["/tmp/a,b]/c.txt"])}.
 * A double quote inside a quoted value is written as {@code \"}, and a
 * backslash as {@code \\}.
 */
public class FeatureParser {

  private static final String PARSE_ERROR = "Could not parse feature. Parameters should be configured with +feature(param[value], param2[value2])";

  public List<FeatureSetting> parseFeatures(Collection<String> config) {
    return asStream(config)
            .map(stringToSettings())
            .collect(Collectors.toList());
  }

  private Function<String, FeatureSetting> stringToSettings() {
    return a -> {
      final String text = a.trim();
      final ToggleStatus status = parseStatus(text);
      final String name = parseName(text);
      return new FeatureSetting(name, status, parseConfig(text));
    };
  }

  private Map<String, List<String>> parseConfig(String a) {
    final Map<String, List<String>> vals = new HashMap<>();
    final int confStart = a.indexOf('(');
    if (confStart == -1) {
      return vals;
    }

    int pos = confStart + 1;
    while (true) {
      pos = skipSeparators(a, pos);
      if (pos >= a.length() || a.charAt(pos) == ')') {
        return vals;
      }

      final int valueStart = indexOfValueStart(a, pos);
      if (valueStart == -1) {
        throw new RuntimeException(PARSE_ERROR);
      }
      final String key = stripSeparators(a.substring(pos, valueStart));

      pos = valueStart + 1;
      final boolean quoted = pos < a.length() && a.charAt(pos) == '"';
      final int valueEnd = quoted ? indexOfQuotedValueEnd(a, pos) : indexOfUnquotedValueEnd(a, pos);
      if (valueEnd == -1) {
        throw new RuntimeException(PARSE_ERROR);
      }

      final String value = quoted
              ? unescape(a.substring(pos + 1, valueEnd))
              : a.substring(pos, valueEnd).trim();
      vals.computeIfAbsent(key, k -> new ArrayList<>()).add(value);

      pos = endOfValue(a, valueEnd, quoted);
    }
  }

  /**
   * Returns the index of the '[' that starts the value of a parameter, or -1 if
   * the parameter has no value.
   */
  private static int indexOfValueStart(String a, int pos) {
    for (int i = pos; i < a.length(); i++) {
      final char c = a.charAt(i);
      if (c == '[') {
        return i;
      }
      if (c == ')' || c == ']') {
        return -1;
      }
    }
    return -1;
  }

  private static int indexOfUnquotedValueEnd(String a, int pos) {
    for (int i = pos; i < a.length(); i++) {
      if (a.charAt(i) == ']') {
        return i;
      }
    }
    return -1;
  }

  /**
   * Returns the index of the quote that closes a quoted value. The opening quote
   * is at {@code pos}.
   */
  private static int indexOfQuotedValueEnd(String a, int pos) {
    for (int i = pos + 1; i < a.length(); i++) {
      final char c = a.charAt(i);
      if (c == '\\' && i + 1 < a.length()) {
        i++;
      } else if (c == '"') {
        return i;
      }
    }
    return -1;
  }

  /**
   * Returns the index just after the ']' that closes a value.
   */
  private static int endOfValue(String a, int valueEnd, boolean quoted) {
    if (!quoted) {
      return valueEnd + 1;
    }
    int pos = valueEnd + 1;
    while (pos < a.length() && Character.isWhitespace(a.charAt(pos))) {
      pos++;
    }
    if (pos < a.length() && a.charAt(pos) == ']') {
      return pos + 1;
    }
    throw new RuntimeException(PARSE_ERROR);
  }

  private static String unescape(String value) {
    final StringBuilder result = new StringBuilder(value.length());
    for (int i = 0; i < value.length(); i++) {
      final char c = value.charAt(i);
      if (c == '\\' && i + 1 < value.length()) {
        final char next = value.charAt(i + 1);
        if (next == '"' || next == '\\') {
          result.append(next);
          i++;
          continue;
        }
      }
      result.append(c);
    }
    return result.toString();
  }

  private static int skipSeparators(String a, int pos) {
    int i = pos;
    while (i < a.length() && (Character.isWhitespace(a.charAt(i)) || a.charAt(i) == ',')) {
      i++;
    }
    return i;
  }

  /**
   * Removes the whitespace and commas that separate a parameter from the
   * one before it. Only whitespace is stripped from values, so commas within
   * a value are preserved.
   */
  private static String stripSeparators(String s) {
    return s.replaceAll("^[\\s,]+|[\\s,]+$", "");
  }

  private String parseName(String a) {
    final String name = a.substring(1);
    final int confStart = name.indexOf('(');
    if (confStart == -1) {
      return name;
    } else {
      return name.substring(0, confStart);
    }
  }

  private ToggleStatus parseStatus(String a) {
    if (a.startsWith("+")) {
      return ToggleStatus.ACTIVATE;
    }
    if (a.startsWith("-")) {
      return ToggleStatus.DEACTIVATE;
    }
    throw new RuntimeException("Could not parse " + a + " should start with + or -");
  }

}
