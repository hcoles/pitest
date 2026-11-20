package org.pitest.aggregate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.pitest.aggregate.TestInvocationHelper.getCompiledDirectory;
import static org.pitest.aggregate.TestInvocationHelper.getSourceDirectory;

import java.io.File;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.pitest.util.ResultOutputStrategy;

public class ReportAggregatorTest {

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  @Test
  public void testAggregateReportWithFullMutationMatrix() throws Exception {
    final File mutationsFile = this.tempFolder.newFile("mutations.xml");
    final String mutationsXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<mutations partial=\"false\">\n"
        + "  <mutation detected='true' status='KILLED' numberOfTestsRun='3'>\n"
        + "    <sourceFile>AggregationResult.java</sourceFile>\n"
        + "    <mutatedClass>org.pitest.aggregate.AggregationResult</mutatedClass>\n"
        + "    <mutatedMethod>getMutations</mutatedMethod>\n"
        + "    <methodDescription>()J</methodDescription>\n"
        + "    <lineNumber>19</lineNumber>\n"
        + "    <mutator>org.pitest.mutationtest.engine.gregor.mutators.returns.PrimitiveReturnsMutator</mutator>\n"
        + "    <indexes><index>4</index></indexes>\n"
        + "    <blocks><block>0</block></blocks>\n"
        + "    <killingTests>com.example.TestA|com.example.TestB</killingTests>\n"
        + "    <succeedingTests>com.example.TestC</succeedingTests>\n"
        + "    <coveringTests>com.example.TestA|com.example.TestB|com.example.TestC</coveringTests>\n"
        + "    <description>replaced long return with 0</description>\n"
        + "  </mutation>\n"
        + "  <mutation detected='false' status='SURVIVED' numberOfTestsRun='2'>\n"
        + "    <sourceFile>AggregationResult.java</sourceFile>\n"
        + "    <mutatedClass>org.pitest.aggregate.AggregationResult</mutatedClass>\n"
        + "    <mutatedMethod>getMutationsSurvived</mutatedMethod>\n"
        + "    <methodDescription>()J</methodDescription>\n"
        + "    <lineNumber>23</lineNumber>\n"
        + "    <mutator>org.pitest.mutationtest.engine.gregor.mutators.returns.PrimitiveReturnsMutator</mutator>\n"
        + "    <indexes><index>4</index></indexes>\n"
        + "    <blocks><block>0</block></blocks>\n"
        + "    <killingTests></killingTests>\n"
        + "    <succeedingTests>com.example.TestC|com.example.TestD</succeedingTests>\n"
        + "    <coveringTests>com.example.TestC|com.example.TestD</coveringTests>\n"
        + "    <description>replaced long return with 0</description>\n"
        + "  </mutation>\n"
        + "</mutations>\n";
    Files.write(mutationsFile.toPath(), mutationsXml.getBytes(StandardCharsets.UTF_8));

    final File coverageFile = this.tempFolder.newFile("linecoverage.xml");
    final String coverageXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<coverage>\n"
        + "  <block classname=\"org.pitest.aggregate.AggregationResult\" method=\"getMutations()J\" number=\"0\" firstInstruction=\"1\" lastInstruction=\"2\">\n"
        + "    <tests>\n"
        + "      <test name=\"com.example.TestA\"/>\n"
        + "    </tests>\n"
        + "  </block>\n"
        + "</coverage>\n";
    Files.write(coverageFile.toPath(), coverageXml.getBytes(StandardCharsets.UTF_8));

    final Map<String, StringWriter> outputs = new HashMap<>();
    final ResultOutputStrategy outputStrategy = sourceFile -> {
      final StringWriter writer = new StringWriter();
      outputs.put(sourceFile.replace(File.separatorChar, '/'), writer);
      return writer;
    };

    final ReportAggregator aggregator = ReportAggregator.builder()
        .resultOutputStrategy(outputStrategy)
        .lineCoverageFiles(Collections.singletonList(coverageFile))
        .mutationResultsFiles(Collections.singletonList(mutationsFile))
        .sourceCodeDirectories(Collections.singletonList(getSourceDirectory()))
        .compiledCodeDirectories(Collections.singletonList(getCompiledDirectory()))
        .build();

    final AggregationResult result = aggregator.aggregateReport();

    assertThat(result.getMutations()).isEqualTo(2);
    assertThat(result.getMutationsSurvived()).isEqualTo(1);

    final String classReportHtml = outputs.get("org.pitest.aggregate/AggregationResult.java.html").toString();
    assertThat(classReportHtml).contains("<b>Killed by : </b>com.example.TestA");
    assertThat(classReportHtml).contains("<b>Killed by : </b>none");
    assertThat(classReportHtml).contains("Covered by tests:");
    assertThat(classReportHtml).contains("<li>com.example.TestC</li>");
    assertThat(classReportHtml).contains("<li>com.example.TestD</li>");
    assertThat(classReportHtml).doesNotContain("<li></li>");
  }

}
