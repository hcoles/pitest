package org.pitest.maven;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.util.Glob;

/**
 * Extracts configuration from surefire plugin and create pitest equivalents
 */
public class SurefireConfigConverter {

  private final boolean parseArgLine;

    public SurefireConfigConverter(boolean parseArgLine) {
        this.parseArgLine = parseArgLine;
    }

    public ReportOptions update(ReportOptions option, Xpp3Dom configuration) {
    if (configuration == null) {
      return option;
    }
    convertExcludes(option, configuration);
    convertGroups(option, configuration);
    convertTestFailureIgnore(option, configuration);
    convertEnvironmentVariables(option, configuration);

    if (parseArgLine) {
      convertArgLine(option, configuration);
    }
    return option;
  }

  private void convertGroups(ReportOptions option, Xpp3Dom configuration) {
    TestGroupConfig existing = option.getGroupConfig();
    if ((existing == null)
        || (existing.getExcludedGroups().isEmpty() && existing
            .getIncludedGroups().isEmpty())) {
      List<String> groups = extractStrings("groups", configuration);
      List<String> excluded = extractStrings("excludedGroups", configuration);
      TestGroupConfig gc = new TestGroupConfig(excluded, groups);
      option.setGroupConfig(gc);
    }
  }

  private List<String> extractStrings(String element, Xpp3Dom configuration) {
    Xpp3Dom groups = configuration.getChild(element);
    if (groups != null) {
      return Arrays.stream(groups.getValue().split("[ ,]+"))
              .filter(StringUtils::isNotBlank)
              .map(String::trim)
              .collect(Collectors.toList());
    } else {
      return Collections.emptyList();
    }
  }

  private void convertExcludes(ReportOptions option, Xpp3Dom configuration) {
    List<Predicate<String>> excludes = new ArrayList<>();
    List<Predicate<String>> surefireExcludes =
            extract("excludes", configuration).stream()
                            .map(p -> p.value)
                            .filter(Objects::nonNull)
                            .map(this::filenameToClassFilter)
                            .collect(Collectors.toList());
    excludes.addAll(surefireExcludes);
    excludes.addAll(option.getExcludedTestClasses());
    option.setExcludedTestClasses(excludes);
  }

  private void convertArgLine(ReportOptions option, Xpp3Dom configuration) {
    Xpp3Dom argLine = configuration.getChild("argLine");
    if (argLine != null) {
      String existing = option.getArgLine() != null ? option.getArgLine() + " " : "";
      option.setArgLine(existing + argLine.getValue());
    }
  }


  private void convertEnvironmentVariables(ReportOptions option, Xpp3Dom configuration) {
    Map<String, String> environmentVariables = extract("environmentVariables", configuration).stream()
            .collect(Collectors.toMap(p -> p.name, p -> p.value));
    option.getEnvironmentVariables().putAll(environmentVariables);
  }

  private Predicate<String> filenameToClassFilter(String filename) {
    return new Glob(filename.replace(".java", "").replace("/", "."));
  }

  private List<Pair> extract(String childname, Xpp3Dom config) {
    final Xpp3Dom subelement = config.getChild(childname);
    if (subelement != null) {
      List<Pair> result = new LinkedList<>();
      final Xpp3Dom[] children = subelement.getChildren();
      for (Xpp3Dom child : children) {
        result.add(new Pair(child.getName(), child.getValue()));
      }
      return result;
    }

    return Collections.emptyList();
  }

  private void convertTestFailureIgnore(ReportOptions option, Xpp3Dom configuration) {
    Xpp3Dom testFailureIgnore = configuration.getChild("testFailureIgnore");
    if (testFailureIgnore != null) {
      option.setSkipFailingTests(Boolean.parseBoolean(testFailureIgnore.getValue()));
    }
  }
}

class Pair {
  final String name;
  final String value;

    Pair(String name, String value) {
        this.name = name;
        this.value = value;
    }
}