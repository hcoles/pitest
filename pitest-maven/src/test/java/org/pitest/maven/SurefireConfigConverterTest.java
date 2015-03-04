package org.pitest.maven;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.junit.Test;
import org.pitest.functional.predicate.Predicate;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.util.Glob;

public class SurefireConfigConverterTest {

  SurefireConfigConverter testee = new SurefireConfigConverter();
  ReportOptions options = new ReportOptions();
  Xpp3Dom surefireConfig;
  
  @Test
  public void shouldIgnoreNullSurefireConfiguration() {
    assertThat(testee.update(options, null)).isSameAs(options);
  }
  
  @Test
  public void shouldCreatePredicateForEachExclude() throws Exception {
    surefireConfig = makeConfig("<excludes><exclude>A</exclude><exclude>B</exclude></excludes>");
    
    ReportOptions actual = testee.update(options, surefireConfig);
    assertThat(actual.getExcludedClasses()).hasSize(2);
  }
  
  @Test
  public void shouldConvertSurefireExclusionsToPackagePredicates() throws Exception {
    surefireConfig = makeConfig("<excludes><exclude>**/FailingTest.java</exclude></excludes>");
   
    ReportOptions actual = testee.update(options, surefireConfig);
    Predicate<String> predicate = actual.getExcludedClasses().iterator().next();
    assertThat(predicate.apply("com.example.FailingTest")).isTrue();
    assertThat(predicate.apply("com.example.Test")).isFalse();
  }
  
  @Test
  public void shouldKeepExistingExclusions() throws Exception {
    surefireConfig = makeConfig("<excludes><exclude>A</exclude><exclude>B</exclude></excludes>");
    options.setExcludedClasses(Collections.<Predicate<String>>singletonList(new Glob("Foo")));
    ReportOptions actual = testee.update(options, surefireConfig);
    
    assertThat(actual.getExcludedClasses()).hasSize(3);
  }

  private Xpp3Dom makeConfig(String s) throws Exception {
    String xml = "<configuration>" + s + "</configuration>";
    InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
    return Xpp3DomBuilder.build(stream, "UTF-8");
  }
  
}
