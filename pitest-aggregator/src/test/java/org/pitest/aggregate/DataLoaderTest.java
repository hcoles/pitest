package org.pitest.aggregate;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Map;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class DataLoaderTest {

  @Test
  public void testLoadMutationSnippet() throws Exception {
    final Document doc = DataLoader.readDocument(DataLoaderTest.class.getResourceAsStream("/snippets/mutation.xml"));
    final Node node = doc.getFirstChild();

    final Map<String, Object> map = DataLoader.nodeMap(node);

    assertEquals(13, map.size());
    assertEquals("true", map.get("detected"));
    assertEquals("KILLED", map.get("status"));
    assertEquals("1", map.get("numberOfTestsRun"));
    assertEquals("OrderedWeightedValueSampler.java", map.get("sourceFile"));
    assertEquals("com.mycompany.OrderedWeightedValueSampler", map.get("mutatedClass"));
    assertEquals("<init>", map.get("mutatedMethod"));
    assertEquals("(JLjava/util/function/Consumer;Ljava/util/function/BiFunction;I)V", map.get("methodDescription"));
    assertEquals("77", map.get("lineNumber"));
    assertEquals("org.pitest.mutationtest.engine.gregor.mutators.MathMutator", map.get("mutator"));
    assertEquals("61", map.get("index"));
    assertEquals(
        "com.mycompany.SmallScaleOrderedWeightedValueSamplerTest.shouldSucceedWithVariousGapTimestamps(com.mycompany.SmallScaleOrderedWeightedValueSamplerTest)",
        map.get("killingTest"));
    assertEquals("Replaced long multiplication with division", map.get("description"));
    assertEquals("27", map.get("block"));
  }

  @Test
  public void testLoadCoverageSnippet() throws Exception {
    final Document doc = DataLoader.readDocument(DataLoaderTest.class.getResourceAsStream("/snippets/linecoverage.xml"));
    final Node node = doc.getFirstChild();

    final Map<String, Object> map = DataLoader.nodeMap(node);

    assertEquals(4, map.size());
    assertEquals("com.example.DividerTest", map.get("classname"));
    assertEquals("testDivide()V", map.get("method"));
    assertEquals("0", map.get("number"));
    assertEquals(Arrays.asList("com.example.DividerTest.testDivide(com.example.DividerTest)"), map.get("tests"));
  }

}
