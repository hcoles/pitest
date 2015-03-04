package org.pitest.maven;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.predicate.Predicate;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.util.Glob;

/**
 * Extracts configuration from surefire plugin
 * and create pitest equivalents
 */
public class SurefireConfigConverter {

  public ReportOptions update(ReportOptions option, Xpp3Dom configuration) {
    if (configuration == null) {
      return option;
    }
    List<Predicate<String>> excludes = FCollection.map(extract("excludes", configuration),filenameToClassFilter());
    excludes.addAll(option.getExcludedClasses());
    option.setExcludedClasses(excludes);
    return option;
  }

  
  private F<String, Predicate<String>> filenameToClassFilter() {
    return new F<String, Predicate<String>>() {
      public Predicate<String> apply(String a) {
        return new Glob(a.replace(".java", "").replace("/", "."));
      }
    };
  }

  private List<String> extract(String childname, Xpp3Dom config) {
    final Xpp3Dom subelement = config.getChild(childname);
    if (subelement != null) {
      List<String> result = new LinkedList<String>();
      final Xpp3Dom[] children = subelement.getChildren();
      for (Xpp3Dom child : children) {
        result.add(child.getValue());
      }
      return result;
    }

    return Collections.<String>emptyList();
  }
  
}
