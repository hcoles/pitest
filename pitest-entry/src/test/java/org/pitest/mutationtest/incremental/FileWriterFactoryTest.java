package org.pitest.mutationtest.incremental;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.pitest.util.FileUtil;
import org.pitest.util.PitError;

public class FileWriterFactoryTest {

  @Rule
  public TemporaryFolder   testFolder = new TemporaryFolder();

  @Rule
  public ExpectedException thrown     = ExpectedException.none();

  @Test
  public void writeToFile() throws IOException {
    final File file = this.testFolder.newFile();
    final FileWriterFactory writerFactory = new FileWriterFactory(file);
    final PrintWriter writer = writerFactory.create();
    writer.write("test");
    writerFactory.close();

    final String content = FileUtil.readToString(new FileInputStream(file));
    assertThat(content, equalTo("test"));
  }

  @Test
  public void writeToFolder() throws IOException {
    this.thrown.expect(PitError.class);
    final Matcher<? extends Throwable> causedBy = instanceOf(IOException.class);
    this.thrown.expectCause(causedBy);

    final File folder = this.testFolder.newFolder();
    final FileWriterFactory writerFactory = new FileWriterFactory(folder);
    writerFactory.create();
  }

  @Test
  public void writeToFileWithinFolder() throws IOException {
    final File folder = this.testFolder.newFolder();
    final File file = new File(folder, "subfolder/file");
    final FileWriterFactory writerFactory = new FileWriterFactory(file);
    final PrintWriter writer = writerFactory.create();
    writer.write("test");
    writerFactory.close();

    final String content = FileUtil.readToString(new FileInputStream(file));
    assertThat(content, equalTo("test"));
  }

}
