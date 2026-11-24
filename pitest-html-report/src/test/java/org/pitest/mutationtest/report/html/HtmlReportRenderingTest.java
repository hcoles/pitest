package org.pitest.mutationtest.report.html;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class HtmlReportRenderingTest {

  @Rule
  public TemporaryFolder temporary = new TemporaryFolder();

  @Test
  public void rendersLinkedReportsWithAssetsAndMutationDetails() throws IOException {
    Path output = Boolean.getBoolean("pit.report.preview")
        ? Path.of("target/report-preview") : temporary.newFolder().toPath();
    HtmlReportPreview.generate(output, true);
    String overview = Files.readString(output.resolve("index.html"));
    assertTrue(overview.contains("12 source files"));
    assertTrue(overview.contains("84.7<span>%</span>"));
    assertTrue(overview.contains("href=\"./com.example.checkout/index.html\""));
    assertTrue(overview.contains("data-value=\"83.3\""));
    assertTrue(Files.size(output.resolve("style.css")) > 0);
    assertTrue(Files.size(output.resolve("report.js")) > 0);
    String pkg = Files.readString(output.resolve("com.example.checkout/index.html"));
    assertTrue(pkg.contains("href=\"./DiscountPolicy.java.html\""));
    String source = Files.readString(output.resolve("com.example.checkout/DiscountPolicy.java.html"));
    assertTrue(source.contains("src=\"../report.js\""));
    assertTrue(source.contains("href=\"#group-9\""));
    assertTrue(source.contains("id=\"group-9\""));
    assertFalse(source.contains("href=\"#group-1\""));
    assertTrue(source.contains("data-mutation-status=\"SURVIVED\""));
    assertTrue(source.contains("data-mutation-status=\"TIMED_OUT\""));
    assertTrue(source.contains("<summary>Covering tests</summary>"));
    assertTrue(source.contains("DiscountPolicyTest.appliesAtMinimum"));
    assertTrue(source.contains("amount &#62;= MINIMUM"));
    assertTrue(source.contains("Mutator <code>org.pitest.mutationtest.engine.gregor.mutators.MathMutator</code>"));
    assertFalse(source.contains("Killed by <code>none</code>"));
    assertTrue(source.contains("legend-dot uncovered"));
    assertFalse(source.contains("onclick="));
  }

  @Test
  public void doesNotShowLineCoverageWhenSuppressed() throws IOException {
    Path output = temporary.newFolder().toPath();
    HtmlReportPreview.generate(output, false);
    String overview = Files.readString(output.resolve("index.html"));
    assertTrue(overview.contains("Line coverage is unavailable"));
    assertTrue(overview.contains("data-value=\"-1\""));
    String source = Files.readString(output.resolve("com.example.checkout/DiscountPolicy.java.html"));
    assertFalse(source.contains("source-code covered"));
    assertFalse(source.contains("legend-dot uncovered"));
    assertTrue(source.contains("data-mutation-status=\"KILLED\""));
  }

  @Test
  public void handlesEmptyProjectAndMissingSource() {
    StringTemplateGroup group = new StringTemplateGroup("rendering_test");
    group.setFileCharEncoding(StandardCharsets.UTF_8.name());
    StringTemplate overview = group.getInstanceOf("templates/mutation/mutation_package_index");
    overview.setAttribute("outputCharset", StandardCharsets.UTF_8);
    overview.setAttribute("totals", new MutationTotals());
    overview.setAttribute("showCoverage", true);
    overview.setAttribute("packageSummaries", Collections.emptyList());
    assertTrue(overview.toString().contains("No mutation results were recorded"));
    StringTemplate source = group.getInstanceOf("templates/mutation/mutation_report");
    source.setAttribute("outputCharset", StandardCharsets.UTF_8);
    source.setAttribute("sourceFile", new SourceFile("Missing.java", Collections.emptyList(), Collections.emptyList()));
    source.setAttribute("totals", new MutationTotals());
    assertTrue(source.toString().contains("Source code could not be located"));
    assertTrue(source.toString().contains("No mutations were recorded"));
  }
}
