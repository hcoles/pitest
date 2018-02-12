package org.pitest.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.pitest.functional.FCollection;

/**
 * Cheap and cheerful parser for simple feature config language.
 *
 * Syntax is designed to require no escaping when embedding in XML
 * document such as a maven pom.
 */
public class FeatureParser {

  public List<FeatureSetting> parseFeatures(Collection<String> config) {
    return FCollection.map(config, stringToSettings());
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
    final int confStart = a.indexOf('(') + 1;
    final int end = a.indexOf(')');
    if ((confStart != -1) && (confStart < end)) {
      final String[] parts = split(a.substring(confStart, end));
      for (final String part : parts) {
        extractValue(part, vals);
      }
    }
    return vals;
  }

  private void extractValue(String part, Map<String, List<String>> vals) {
    final String[] pairs = part.split("\\[");
    for (int i = 0; i != pairs.length; i = i + 2) {
      final String key = pairs[i].trim();
      List<String> current = vals.get(key);
      if (current == null) {
        current = new ArrayList<>();
      }
      current.add(pairs[i + 1].trim());
     vals.put(key, current);
    }
  }

  private String[] split(String body) {
    return body.split("\\]");
  }

  private String parseName(String a) {
    final String name = a.substring(1, a.length());
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
