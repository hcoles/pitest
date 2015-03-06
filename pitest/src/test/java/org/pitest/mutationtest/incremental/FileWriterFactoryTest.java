package org.pitest.mutationtest.incremental;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.pitest.util.PitError;

public class FileWriterFactoryTest {

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void writeToFile() throws IOException {
		File file = testFolder.newFile();
		FileWriterFactory writerFactory = new FileWriterFactory(file);
		PrintWriter writer = writerFactory.create();
		writer.write("test");
		writerFactory.close();

		byte[] content = Files.readAllBytes(file.toPath());
		assertThat(new String(content), equalTo("test"));
	}

	@Test
	public void writeToFolder() throws IOException {
		thrown.expect(PitError.class);
		Matcher<? extends Throwable> causedBy = instanceOf(IOException.class);
		thrown.expectCause(causedBy);

		File folder = testFolder.newFolder();
		FileWriterFactory writerFactory = new FileWriterFactory(folder);
		writerFactory.create();
	}

	@Test
	public void writeToFileWithinFolder() throws IOException {
		File folder = testFolder.newFolder();
		File file = new File(folder, "subfolder/file");
		FileWriterFactory writerFactory = new FileWriterFactory(file);
		PrintWriter writer = writerFactory.create();
		writer.write("test");
		writerFactory.close();

		byte[] content = Files.readAllBytes(file.toPath());
		assertThat(new String(content), equalTo("test"));
	}
	
}
