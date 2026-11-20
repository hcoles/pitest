package org.pitest.aggregate;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.ClassMutationResults;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.report.xml.XMLReportListener;

public class MutationResultDataLoaderTest {

  private MutationResultDataLoader underTest;

  @Before
  public void setup() throws Exception {
    final URL url = MutationResultDataLoaderTest.class.getResource("/full-data/mutations.xml");
    final File file = new File(url.toURI());

    this.underTest = new MutationResultDataLoader(Arrays.asList(file));
  }

  @Test
  public void testLoadData() throws Exception {
    final Collection<MutationResult> results = this.underTest.loadData();

    assertThat(results).isNotNull();
    assertThat(results).hasSize(2);

    for (final MutationResult result : results) {
      if (result.getDetails().getFirstIndex() == 5) {
        assertThat(result.getDetails().getBlocks()).contains(38);
        assertThat(result.getDetails().getId().getClassName().asJavaName()).isEqualTo("com.mycompany.OrderedWeightedValueSampler");
        assertThat(result.getDetails().getLineNumber()).isEqualTo(202);
        assertThat(result.getStatus().isDetected()).isFalse();
        assertThat(result.getStatus()).isEqualTo(DetectionStatus.NO_COVERAGE);
        assertThat(result.getKillingTest()).isEmpty();
        assertThat(result.getKillingTestDescription()).isEqualTo("none");
        assertThat(result.getKillingTests()).isEmpty();
        assertThat(result.getSucceedingTests()).isEmpty();
        assertThat(result.getCoveringTests()).isEmpty();
      } else {
        assertThat(result.getDetails().getBlocks()).contains(27);
        assertThat(result.getDetails().getId().getClassName().asJavaName()).isEqualTo("com.mycompany.OrderedWeightedValueSampler");
        assertThat(result.getDetails().getLineNumber()).isEqualTo(77);
        assertThat(result.getStatus().isDetected()).isTrue();
        assertThat(result.getStatus()).isEqualTo(DetectionStatus.KILLED);
        assertThat(result.getKillingTest()).contains(
            "com.mycompany.SmallScaleOrderedWeightedValueSamplerTest.shouldSucceedWithVariousGapTimestamps(com.mycompany.SmallScaleOrderedWeightedValueSamplerTest)");
        assertThat(result.getKillingTests()).containsExactly(
            "com.mycompany.SmallScaleOrderedWeightedValueSamplerTest.shouldSucceedWithVariousGapTimestamps(com.mycompany.SmallScaleOrderedWeightedValueSamplerTest)");
        assertThat(result.getCoveringTests()).containsExactly(
            "com.mycompany.SmallScaleOrderedWeightedValueSamplerTest.shouldSucceedWithVariousGapTimestamps(com.mycompany.SmallScaleOrderedWeightedValueSamplerTest)");
      }
    }
  }

  @Test
  public void testLoadFullMutationMatrixData() throws Exception {
    final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<mutations partial=\"false\">\n"
        + "  <mutation detected='true' status='KILLED' numberOfTestsRun='4'>\n"
        + "    <sourceFile>Sample.java</sourceFile>\n"
        + "    <mutatedClass>com.example.Sample</mutatedClass>\n"
        + "    <mutatedMethod>add</mutatedMethod>\n"
        + "    <methodDescription>(II)I</methodDescription>\n"
        + "    <lineNumber>10</lineNumber>\n"
        + "    <mutator>MathMutator</mutator>\n"
        + "    <indexes><index>1</index></indexes>\n"
        + "    <blocks><block>0</block></blocks>\n"
        + "    <killingTests>testA|testB</killingTests>\n"
        + "    <succeedingTests>testC|testD</succeedingTests>\n"
        + "    <coveringTests>testA|testB|testC|testD</coveringTests>\n"
        + "    <description>Replaced integer addition with subtraction</description>\n"
        + "  </mutation>\n"
        + "  <mutation detected='false' status='SURVIVED' numberOfTestsRun='2'>\n"
        + "    <sourceFile>Sample.java</sourceFile>\n"
        + "    <mutatedClass>com.example.Sample</mutatedClass>\n"
        + "    <mutatedMethod>sub</mutatedMethod>\n"
        + "    <methodDescription>(II)I</methodDescription>\n"
        + "    <lineNumber>15</lineNumber>\n"
        + "    <mutator>MathMutator</mutator>\n"
        + "    <indexes><index>2</index></indexes>\n"
        + "    <blocks><block>1</block></blocks>\n"
        + "    <killingTests></killingTests>\n"
        + "    <succeedingTests>testOne(int, String)|testTwo&lt;T&gt;&amp;More</succeedingTests>\n"
        + "    <coveringTests>testOne(int, String)|testTwo&lt;T&gt;&amp;More</coveringTests>\n"
        + "    <description>Replaced integer subtraction with addition</description>\n"
        + "  </mutation>\n"
        + "  <mutation detected='false' status='NO_COVERAGE' numberOfTestsRun='0'>\n"
        + "    <sourceFile>Sample.java</sourceFile>\n"
        + "    <mutatedClass>com.example.Sample</mutatedClass>\n"
        + "    <mutatedMethod>mul</mutatedMethod>\n"
        + "    <methodDescription>(II)I</methodDescription>\n"
        + "    <lineNumber>20</lineNumber>\n"
        + "    <mutator>MathMutator</mutator>\n"
        + "    <indexes><index>3</index></indexes>\n"
        + "    <blocks><block>2</block></blocks>\n"
        + "    <killingTests/>\n"
        + "    <succeedingTests/>\n"
        + "    <coveringTests/>\n"
        + "    <description>Replaced integer multiplication with division</description>\n"
        + "  </mutation>\n"
        + "</mutations>\n";

    final Set<MutationResult> results = this.underTest.loadData(
        new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)),
        new File("mutations.xml"));

    assertThat(results).hasSize(3);

    for (final MutationResult result : results) {
      final String method = result.getDetails().getMethod();
      if ("add".equals(method)) {
        assertThat(result.getStatus()).isEqualTo(DetectionStatus.KILLED);
        assertThat(result.getNumberOfTestsRun()).isEqualTo(4);
        assertThat(result.getKillingTest()).contains("testA");
        assertThat(result.getKillingTests()).containsExactly("testA", "testB");
        assertThat(result.getSucceedingTests()).containsExactly("testC", "testD");
        assertThat(result.getCoveringTests()).containsExactly("testA", "testB", "testC", "testD");
      } else if ("sub".equals(method)) {
        assertThat(result.getStatus()).isEqualTo(DetectionStatus.SURVIVED);
        assertThat(result.getNumberOfTestsRun()).isEqualTo(2);
        assertThat(result.getKillingTest()).isEmpty();
        assertThat(result.getKillingTestDescription()).isEqualTo("none");
        assertThat(result.getKillingTests()).isEmpty();
        assertThat(result.getSucceedingTests()).containsExactly("testOne(int, String)", "testTwo<T>&More");
        assertThat(result.getCoveringTests()).containsExactly("testOne(int, String)", "testTwo<T>&More");
      } else {
        assertThat(method).isEqualTo("mul");
        assertThat(result.getStatus()).isEqualTo(DetectionStatus.NO_COVERAGE);
        assertThat(result.getKillingTest()).isEmpty();
        assertThat(result.getKillingTestDescription()).isEqualTo("none");
        assertThat(result.getKillingTests()).isEmpty();
        assertThat(result.getSucceedingTests()).isEmpty();
        assertThat(result.getCoveringTests()).isEmpty();
      }
    }
  }

  @Test
  public void testRoundTripWithXmlReportListenerFullMutationMatrix() throws Exception {
    final MutationResult killed = new MutationResult(
        new MutationDetails(
            new MutationIdentifier(
                new Location(ClassName.fromString("com.example.Calc"), "add", "(II)I"),
                Arrays.asList(1, 2),
                "MathMutator"),
            "Calc.java",
            "Replaced integer addition with subtraction",
            12,
            Arrays.asList(0)),
        new MutationStatusTestPair(
            3,
            DetectionStatus.KILLED,
            Arrays.asList("com.example.CalcTest.testAdd(int, int)", "com.example.CalcTest.testEdge<1>&2"),
            Arrays.asList("com.example.CalcTest.testOther"),
            Arrays.asList("com.example.CalcTest.testAdd(int, int)", "com.example.CalcTest.testEdge<1>&2", "com.example.CalcTest.testOther")));

    final MutationResult survived = new MutationResult(
        new MutationDetails(
            new MutationIdentifier(
                new Location(ClassName.fromString("com.example.Calc"), "sub", "(II)I"),
                Collections.singletonList(4),
                "MathMutator"),
            "Calc.java",
            "Replaced integer subtraction with addition",
            16,
            Collections.singletonList(1)),
        new MutationStatusTestPair(
            1,
            DetectionStatus.SURVIVED,
            Collections.emptyList(),
            Collections.singletonList("com.example.CalcTest.testSub(int, int)"),
            Collections.singletonList("com.example.CalcTest.testSub(int, int)")));

    final MutationResult noCoverage = new MutationResult(
        new MutationDetails(
            new MutationIdentifier(
                new Location(ClassName.fromString("com.example.Calc"), "mul", "(II)I"),
                Collections.singletonList(8),
                "MathMutator"),
            "Calc.java",
            "Replaced integer multiplication with division",
            20,
            Collections.singletonList(2)),
        MutationStatusTestPair.notAnalysed(0, DetectionStatus.NO_COVERAGE, Collections.emptyList()));

    final StringWriter writer = new StringWriter();
    final XMLReportListener listener = new XMLReportListener(writer, true, false);
    listener.runStart();
    listener.handleMutationResult(new ClassMutationResults(Arrays.asList(killed, survived, noCoverage)));
    listener.runEnd();

    final Set<MutationResult> loaded = this.underTest.loadData(
        new ByteArrayInputStream(writer.toString().getBytes(StandardCharsets.UTF_8)),
        new File("mutations.xml"));

    assertThat(loaded).containsExactlyInAnyOrder(killed, survived, noCoverage);
  }

  @Test
  public void testIgnoresBlankTagsAndEmptySegments() throws Exception {
    final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<mutations partial=\"false\">\n"
        + "  <mutation detected='false' status='SURVIVED' numberOfTestsRun='1'>\n"
        + "    <sourceFile>Sample.java</sourceFile>\n"
        + "    <mutatedClass>com.example.Sample</mutatedClass>\n"
        + "    <mutatedMethod>blankLegacy</mutatedMethod>\n"
        + "    <methodDescription>()V</methodDescription>\n"
        + "    <lineNumber>10</lineNumber>\n"
        + "    <mutator>MathMutator</mutator>\n"
        + "    <indexes><index>1</index></indexes>\n"
        + "    <blocks><block>0</block></blocks>\n"
        + "    <killingTest>   </killingTest>\n"
        + "    <description>desc</description>\n"
        + "  </mutation>\n"
        + "  <mutation detected='true' status='KILLED' numberOfTestsRun='2'>\n"
        + "    <sourceFile>Sample.java</sourceFile>\n"
        + "    <mutatedClass>com.example.Sample</mutatedClass>\n"
        + "    <mutatedMethod>matrixSegments</mutatedMethod>\n"
        + "    <methodDescription>()V</methodDescription>\n"
        + "    <lineNumber>20</lineNumber>\n"
        + "    <mutator>MathMutator</mutator>\n"
        + "    <indexes><index>2</index></indexes>\n"
        + "    <blocks><block>1</block></blocks>\n"
        + "    <killingTests> |testA||testB| </killingTests>\n"
        + "    <succeedingTests>   </succeedingTests>\n"
        + "    <coveringTests>testA||testB|</coveringTests>\n"
        + "    <description>desc</description>\n"
        + "  </mutation>\n"
        + "</mutations>\n";

    final Set<MutationResult> results = this.underTest.loadData(
        new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)),
        new File("mutations.xml"));

    assertThat(results).hasSize(2);
    for (final MutationResult result : results) {
      if ("blankLegacy".equals(result.getDetails().getMethod())) {
        assertThat(result.getKillingTest()).isEmpty();
        assertThat(result.getKillingTestDescription()).isEqualTo("none");
        assertThat(result.getKillingTests()).isEmpty();
        assertThat(result.getCoveringTests()).isEmpty();
      } else {
        assertThat(result.getKillingTest()).contains("testA");
        assertThat(result.getKillingTests()).containsExactly("testA", "testB");
        assertThat(result.getSucceedingTests()).isEmpty();
        assertThat(result.getCoveringTests()).containsExactly("testA", "testB");
      }
    }
  }

}
