package org.pitest.maven;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import java.util.function.Function;
import org.pitest.functional.FCollection;
import java.util.function.Predicate;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.util.Glob;

/**
 * Extracts configuration from surefire plugin and create pitest equivalents
 */
public class SurefireConfigConverter {

  public ReportOptions update(ReportOptions option, Xpp3Dom configuration) {
    if (configuration == null) {
      return option;
    }
    convertExcludes(option, configuration);
    convertGroups(option, configuration);
    convertTestFailureIgnore(option, configuration);
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
      String[] parts = groups.getValue().split(" ");
      return Arrays.asList(parts);
    } else {
      return Collections.emptyList();
    }
  }

  private void convertExcludes(ReportOptions option, Xpp3Dom configuration) {
    List<Predicate<String>> excludes = FCollection.map(
        extract("excludes", configuration), filenameToClassFilter());
    excludes.addAll(option.getExcludedTestClasses());
    option.setExcludedTestClasses(excludes);
  }

  private Function<String, Predicate<String>> filenameToClassFilter() {
    return new Function<String, Predicate<String>>() {
      @Override
      public Predicate<String> apply(String a) {
        return new Glob(a.replace(".java", "").replace("/", "."));
      }
    };
  }

  private List<String> extract(String childname, Xpp3Dom config) {
    final Xpp3Dom subelement = config.getChild(childname);
    if (subelement != null) {
      List<String> result = new LinkedList<>();
      final Xpp3Dom[] children = subelement.getChildren();
      for (Xpp3Dom child : children) {
        result.add(child.getValue());
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
