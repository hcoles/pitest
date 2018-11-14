package org.pitest.maven;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.junit.Test;
import java.util.function.Predicate;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.util.Glob;

public class SurefireConfigConverterTest {

  SurefireConfigConverter testee  = new SurefireConfigConverter();
  ReportOptions           options = new ReportOptions();
  Xpp3Dom                 surefireConfig;

  @Test
  public void shouldIgnoreNullSurefireConfiguration() {
    assertThat(this.testee.update(this.options, null)).isSameAs(this.options);
  }

  @Test
  public void shouldCreatePredicateForEachExclude() throws Exception {
    this.surefireConfig = makeConfig("<excludes><exclude>A</exclude><exclude>B</exclude></excludes>");

    ReportOptions actual = this.testee
        .update(this.options, this.surefireConfig);
    assertThat(actual.getExcludedTestClasses()).hasSize(2);
  }

  @Test
  public void shouldConvertSurefireExclusionsToPackagePredicates()
      throws Exception {
    this.surefireConfig = makeConfig("<excludes><exclude>**/FailingTest.java</exclude></excludes>");

    ReportOptions actual = this.testee
        .update(this.options, this.surefireConfig);
    Predicate<String> predicate = actual.getExcludedTestClasses().iterator().next();
    assertThat(predicate.test("com.example.FailingTest")).isTrue();
    assertThat(predicate.test("com.example.Test")).isFalse();
  }

  @Test
  public void shouldKeepExistingExclusions() throws Exception {
    this.surefireConfig = makeConfig("<excludes><exclude>A</exclude><exclude>B</exclude></excludes>");
    this.options.setExcludedTestClasses(Collections
        .<Predicate<String>> singletonList(new Glob("Foo")));
    ReportOptions actual = this.testee
        .update(this.options, this.surefireConfig);

    assertThat(actual.getExcludedTestClasses()).hasSize(3);
  }

  @Test
  public void shouldConvertSingleSurefireGroups() throws Exception {
    this.surefireConfig = makeConfig("<groups>com.example.Unit</groups>");
    ReportOptions actual = this.testee
        .update(this.options, this.surefireConfig);

    assertThat(actual.getGroupConfig().getIncludedGroups()).containsOnly(
        "com.example.Unit");
  }

  @Test
  public void shouldConvertMultipleSurefireGroups() throws Exception {
    this.surefireConfig = makeConfig("<groups>com.example.Unit com.example.Fast</groups>");
    ReportOptions actual = this.testee
        .update(this.options, this.surefireConfig);

    assertThat(actual.getGroupConfig().getIncludedGroups()).containsOnly(
        "com.example.Unit", "com.example.Fast");
  }

  @Test
  public void shouldConvertMultipleSurefireGroupExcludes() throws Exception {
    this.surefireConfig = makeConfig("<excludedGroups>com.example.Unit com.example.Fast</excludedGroups>");
    ReportOptions actual = this.testee
        .update(this.options, this.surefireConfig);

    assertThat(actual.getGroupConfig().getExcludedGroups()).containsOnly(
        "com.example.Unit", "com.example.Fast");
  }

  @Test
  public void shouldNotUseSurefireGroupsWhenPitestIncludesSpecified()
      throws Exception {
    TestGroupConfig gc = new TestGroupConfig(Collections.<String> emptyList(),
        Arrays.asList("bar"));
    this.options.setGroupConfig(gc);
    this.surefireConfig = makeConfig("<groups>com.example.Unit com.example.Fast</groups>");
    ReportOptions actual = this.testee
        .update(this.options, this.surefireConfig);

    assertThat(actual.getGroupConfig().getIncludedGroups()).containsOnly("bar");
  }

  @Test
  public void shouldNotUseSurefireGroupsWhenPitestExcludesSpecified()
      throws Exception {
    TestGroupConfig gc = new TestGroupConfig(Arrays.asList("bar"),
        Collections.<String> emptyList());
    this.options.setGroupConfig(gc);
    this.surefireConfig = makeConfig("<groups>com.example.Unit com.example.Fast</groups>");

    ReportOptions actual = this.testee
        .update(this.options, this.surefireConfig);

    assertThat(actual.getGroupConfig().getExcludedGroups()).containsOnly("bar");
  }

  @Test
  public void shouldConvertTestFailureIgnoreWhenTrue() throws Exception {
    this.surefireConfig = makeConfig("<testFailureIgnore>true</testFailureIgnore>");

    ReportOptions actual = this.testee
        .update(this.options, this.surefireConfig);

    assertThat(actual.skipFailingTests()).isTrue();
  }

  @Test
  public void shouldConvertTestFailureIgnoreWhenFalse() throws Exception {
    this.surefireConfig = makeConfig("<testFailureIgnore>false</testFailureIgnore>");

    ReportOptions actual = this.testee
        .update(this.options, this.surefireConfig);

    assertThat(actual.skipFailingTests()).isFalse();
  }

  @Test
  public void shouldConvertTestFailureIgnoreWhenAbsent() throws Exception {
    this.surefireConfig = makeConfig("<testFailureIgnore></testFailureIgnore>");

    ReportOptions actual = this.testee
        .update(this.options, this.surefireConfig);

    assertThat(actual.skipFailingTests()).isFalse();
  }

  private Xpp3Dom makeConfig(String s) throws Exception {
    String xml = "<configuration>" + s + "</configuration>";
    InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
    return Xpp3DomBuilder.build(stream, "UTF-8");
  }

}
