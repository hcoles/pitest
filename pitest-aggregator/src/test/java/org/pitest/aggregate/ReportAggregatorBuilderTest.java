package org.pitest.aggregate;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.pitest.aggregate.TestInvocationHelper.getCompiledDirectory;
import static org.pitest.aggregate.TestInvocationHelper.getCoverageFile;
import static org.pitest.aggregate.TestInvocationHelper.getMutationFile;
import static org.pitest.aggregate.TestInvocationHelper.getResultOutputStrategy;
import static org.pitest.aggregate.TestInvocationHelper.getSourceDirectory;
import static org.pitest.aggregate.TestInvocationHelper.getTestCompiledDirectory;
import static org.pitest.aggregate.TestInvocationHelper.getTestSourceDirectory;

import java.io.File;
import java.util.Arrays;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ReportAggregatorBuilderTest {

  private static final String NOT_A_FILE = "does not exist or is not a file";
  private static final String NOT_A_DIR  = "is not a directory";
  private static final String IS_NULL    = "is null";
  @Rule
  public ExpectedException    expected   = ExpectedException.none();

  @Test
  public void testLineCoverageFiles_withNull() {
    this.expected.expect(IllegalArgumentException.class);
    this.expected.expectMessage(Matchers.containsString(IS_NULL));

    ReportAggregator.builder().lineCoverageFiles(Arrays.asList(getCoverageFile(), null, getCoverageFile()));
  }

  @Test
  public void testLineCoverageFiles_withFake() {
    this.expected.expect(IllegalArgumentException.class);
    this.expected.expectMessage(Matchers.containsString(NOT_A_FILE));

    ReportAggregator.builder().lineCoverageFiles(Arrays.asList(new File("doesnotexist.xml")));
  }

  @Test
  public void testLineCoverageFiles_withDir() {
    this.expected.expect(IllegalArgumentException.class);
    this.expected.expectMessage(Matchers.containsString(NOT_A_FILE));

    ReportAggregator.builder().lineCoverageFiles(Arrays.asList(getSourceDirectory()));
  }

  @Test
  public void testMutationResultsFiles_withNull() {
    this.expected.expect(IllegalArgumentException.class);
    this.expected.expectMessage(Matchers.containsString(IS_NULL));

    ReportAggregator.builder().mutationResultsFiles(Arrays.asList(getMutationFile(), null, getMutationFile()));
  }

  @Test
  public void testMutationResultsFiles_withFake() {
    this.expected.expect(IllegalArgumentException.class);
    this.expected.expectMessage(Matchers.containsString(NOT_A_FILE));

    ReportAggregator.builder().mutationResultsFiles(Arrays.asList(new File("doesnotexist.xml")));
  }

  @Test
  public void testMutationResultsFiles_withDir() {
    this.expected.expect(IllegalArgumentException.class);
    this.expected.expectMessage(Matchers.containsString(NOT_A_FILE));

    ReportAggregator.builder().mutationResultsFiles(Arrays.asList(getTestSourceDirectory()));
  }

  @Test
  public void testSourceCodeDirectories_withNull() {
    this.expected.expect(IllegalArgumentException.class);
    this.expected.expectMessage(Matchers.containsString(IS_NULL));

    ReportAggregator.builder().sourceCodeDirectories(Arrays.asList(getSourceDirectory(), null, getTestSourceDirectory()));
  }

  @Test
  public void testSourceCodeDirectories_withFake() {
    ReportAggregator.Builder builder = ReportAggregator.builder()
        .sourceCodeDirectories(Arrays.asList(new File("fakedirectory")));

    assertTrue(builder.getSourceCodeDirectories().isEmpty());
  }

  @Test
  public void testSourceCodeDirectories_withFile() {
    this.expected.expect(IllegalArgumentException.class);
    this.expected.expectMessage(Matchers.containsString(NOT_A_DIR));

    ReportAggregator.builder().sourceCodeDirectories(Arrays.asList(getCoverageFile()));
  }

  @Test
  public void testCompiledCodeDirectories_withNull() {
    this.expected.expect(IllegalArgumentException.class);
    this.expected.expectMessage(Matchers.containsString(IS_NULL));

    ReportAggregator.builder().compiledCodeDirectories(Arrays.asList(getCompiledDirectory(), null, getTestCompiledDirectory()));
  }

  @Test
  public void testCompiledCodeDirectories_withFake() {
    ReportAggregator.Builder builder = ReportAggregator.builder()
        .compiledCodeDirectories(Arrays.asList(new File("fakedirectory")));

    assertTrue(builder.getCompiledCodeDirectories().isEmpty());
  }

  @Test
  public void testCompiledCodeDirectories_withFile() {
    this.expected.expect(IllegalArgumentException.class);
    this.expected.expectMessage(Matchers.containsString(NOT_A_DIR));

    ReportAggregator.builder().compiledCodeDirectories(Arrays.asList(getMutationFile()));
  }

  @Test
  public void testBuild() {
    assertNotNull(ReportAggregator.builder() // create builder
        .resultOutputStrategy(getResultOutputStrategy()) // resultOutputStrategy
        .lineCoverageFiles(Arrays.asList(getCoverageFile())) // lineCoverageFiles
        .mutationResultsFiles(Arrays.asList(getMutationFile())) // mutationResultsFiles
        .compiledCodeDirectories(Arrays.asList(getCompiledDirectory(), getTestCompiledDirectory())) // compiledCodeDirectories
        .sourceCodeDirectories(Arrays.asList(getSourceDirectory(), getTestSourceDirectory())) // sourceCodeDirectories
        .build());
  }

  @Test
  public void testBuild_missingOutputStrategy() {
    this.expected.expect(IllegalStateException.class);
    this.expected.expectMessage(Matchers.containsString("resultOutputStrategy"));
    ReportAggregator.builder() // create builder
        .lineCoverageFiles(Arrays.asList(getCoverageFile())) // lineCoverageFiles
        .mutationResultsFiles(Arrays.asList(getMutationFile())) // mutationResultsFiles
        .compiledCodeDirectories(Arrays.asList(getCompiledDirectory(), getTestCompiledDirectory())) // compiledCodeDirectories
        .sourceCodeDirectories(Arrays.asList(getSourceDirectory(), getTestSourceDirectory())) // sourceCodeDirectories
        .build();
  }

  @Test
  public void testBuild_missingCoverageFiles() {
    this.expected.expect(IllegalStateException.class);
    this.expected.expectMessage(Matchers.containsString("lineCoverageFiles"));
    ReportAggregator.builder() // create builder
        .resultOutputStrategy(getResultOutputStrategy()) // resultOutputStrategy
        .mutationResultsFiles(Arrays.asList(getMutationFile())) // mutationResultsFiles
        .compiledCodeDirectories(Arrays.asList(getCompiledDirectory(), getTestCompiledDirectory())) // compiledCodeDirectories
        .sourceCodeDirectories(Arrays.asList(getSourceDirectory(), getTestSourceDirectory())) // sourceCodeDirectories
        .build();
  }

  @Test
  public void testBuild_missingMutationResultsFiles() {
    this.expected.expect(IllegalStateException.class);
    this.expected.expectMessage(Matchers.containsString("mutationResultsFiles"));
    ReportAggregator.builder() // create builder
        .resultOutputStrategy(getResultOutputStrategy()) // resultOutputStrategy
        .lineCoverageFiles(Arrays.asList(getCoverageFile())) // lineCoverageFiles
        .compiledCodeDirectories(Arrays.asList(getCompiledDirectory(), getTestCompiledDirectory())) // compiledCodeDirectories
        .sourceCodeDirectories(Arrays.asList(getSourceDirectory(), getTestSourceDirectory())) // sourceCodeDirectories
        .build();
  }

  @Test
  public void testBuild_missingCompiledCodeDirectories() {
    this.expected.expect(IllegalStateException.class);
    this.expected.expectMessage(Matchers.containsString("compiledCodeDirectories"));
    ReportAggregator.builder() // create builder
        .resultOutputStrategy(getResultOutputStrategy()) // resultOutputStrategy
        .lineCoverageFiles(Arrays.asList(getCoverageFile())) // lineCoverageFiles
        .mutationResultsFiles(Arrays.asList(getMutationFile())) // mutationResultsFiles
        .sourceCodeDirectories(Arrays.asList(getSourceDirectory(), getTestSourceDirectory())) // sourceCodeDirectories
        .build();
  }

  @Test
  public void testBuild_missingSourceCodeDirectories() {
    this.expected.expect(IllegalStateException.class);
    this.expected.expectMessage(Matchers.containsString("sourceCodeDirectories"));
    ReportAggregator.builder() // create builder
        .resultOutputStrategy(getResultOutputStrategy()) // resultOutputStrategy
        .lineCoverageFiles(Arrays.asList(getCoverageFile())) // lineCoverageFiles
        .mutationResultsFiles(Arrays.asList(getMutationFile())) // mutationResultsFiles
        .compiledCodeDirectories(Arrays.asList(getCompiledDirectory(), getTestCompiledDirectory())) // compiledCodeDirectories
        .build();
  }

}
