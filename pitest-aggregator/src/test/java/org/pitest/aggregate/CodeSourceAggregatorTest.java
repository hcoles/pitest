package org.pitest.aggregate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.CodeSource;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.config.SettingsFactory;

public class CodeSourceAggregatorTest {

  private CodeSourceAggregator underTest;

  @Before
  public void setup() throws Exception {
    final File testDir = new File(CodeSourceAggregatorTest.class.getResource("/").toURI());
    final File mainDir = new File(CodeSourceAggregatorTest.class.getResource("/org/pitest/aggregate/DataLoader.class").toURI()).getParentFile() // aggregate
        .getParentFile() // pitest
        .getParentFile() // org
        .getParentFile(); // classes
    SettingsFactory f = new SettingsFactory(new ReportOptions(), PluginServices.makeForContextLoader());
    this.underTest = new CodeSourceAggregator(f, Arrays.asList(testDir, mainDir));
  }

  @Test
  public void testCreateCodeSource() {
    final CodeSource source = this.underTest.createCodeSource();
    assertNotNull(source);

    assertTrue(source.fetchClassHash(ClassName.fromClass(CodeSourceAggregator.class)).isPresent());
    assertFalse(source.fetchClassHash(ClassName.fromString("com.doesnt.exist.Type")).isPresent());
  }

}
