/*
 * Copyright 2011 Henry Coles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest.mutationtest.tooling;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DirectorySourceLocatorTest {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();
  Path root;

  private DirectorySourceLocator testee;

  @Before
  public void setUp() {
    root = folder.getRoot().toPath();
    this.testee = new DirectorySourceLocator(this.root, StandardCharsets.UTF_8);
  }

  @Test
  public void locatesSourceForClassesInDefaultPackage() throws Exception {
    createFile(root.resolve("Foo.java"), "foo");
    createFile(root.resolve("Bar.java"), "bar");
    Optional<Reader> actual = testee.locate(singletonList("Foo"), "Foo.java");
    assertThat(actual).isPresent();
    assertThat(content(actual)).isEqualTo("foo");
  }

  @Test
  public void locatesSourceForClassesInNamedPackages()  throws Exception  {
    createFile(root.resolve("com/example/Foo.java"), "foo");
    createFile(root.resolve("com/example/Bar.java"), "bar");
    Optional<Reader> actual = testee.locate(singletonList("com.example.Foo"), "Foo.java");
    assertThat(content(actual)).isEqualTo("foo");
  }

  @Test
  public void findsFileInPackageBeforeOneAtRoot()  throws Exception  {
    createFile(root.resolve("com/example/Foo.java"), "this one");
    createFile(root.resolve("Foo.java"), "not this one");
    Optional<Reader> actual = testee.locate(singletonList("com.example.Foo"), "Foo.java");
    assertThat(content(actual)).isEqualTo("this one");
  }

  @Test
  public void findsFileInCorrectPackageBeforeWronglyPackagedOnes()  throws Exception  {
    createFile(root.resolve("com/example/correct/Foo.java"), "correct");
    createFile(root.resolve("Foo.java"), "in package default");
    createFile(root.resolve("com/example/Foo.java"), "example");
    createFile(root.resolve("com/example/wrong/Foo.java"), "not this one");
    createFile(root.resolve("com/example/correct/wrong/Foo.java"), "not this one");

    assertThat(findFor("com.example.correct.Foo", "Foo.java")).isEqualTo("correct");
    assertThat(findFor("Foo", "Foo.java")).isEqualTo("in package default");
    assertThat(findFor("com.example.Foo", "Foo.java")).isEqualTo("example");
  }

  @Test
  @Ignore
  // Docs suggest that Files.walk/find should search depth first, but behaviour seems
  // to be OS dependent in practice. Windows ci on azure looks to search depth first, linux
  // and mac find the root file. Fortunately we don't actually care about the behaviour in this case
  // either file might be the one the user intended
  public void findsFileInWrongPackageBeforeRoot()  throws Exception  {
    createFile(root.resolve("com/example/other/Foo.java"), "this one");
    createFile(root.resolve("Foo.java"), "not this one");
    Optional<Reader> actual = testee.locate(singletonList("com.example.Foo"), "Foo.java");
    assertThat(content(actual)).isEqualTo("this one");
  }

  @Test
  public void usesFileInRightPAckage()  throws Exception  {
    createFile(root.resolve("com/example/other/Foo.java"), "not this one");
    createFile(root.resolve("com/example/correct/Foo.java"), "this one");
    Optional<Reader> actual = testee.locate(singletonList("com.example.correct.Foo"), "Foo.java");
    assertThat(content(actual)).isEqualTo("this one");
  }

  @Test
  public void doesNotTryToReadDirectories()  throws Exception  {
    Files.createDirectories(root.resolve("com/example/Foo.java"));
    createFile(root.resolve("Foo.java"), "this one");
    Optional<Reader> actual = testee.locate(singletonList("com.example.Foo"), "Foo.java");
    assertThat(content(actual)).isEqualTo("this one");
  }

  @Test
  public void doesNotErrorWhenRootDoesNotExist() {
    testee = new DirectorySourceLocator(this.root.resolve("doesNotExist"), StandardCharsets.UTF_8);
    assertThatCode(() -> testee.locate(singletonList("com"), "Bar.java"))
            .doesNotThrowAnyException();
  }

  @Test
  public void toStringGivesRootsPath() {
    Path path =  this.root.resolve("some/path");
    testee = new DirectorySourceLocator(path, StandardCharsets.UTF_8);
    assertThat(testee.toString()).isEqualTo(path.toString());
  }

  private String findFor(String clazz, String file) throws Exception {
    return findFor(singletonList(clazz), file);
  }

  private String findFor(Collection<String> classes, String file) throws Exception {
    Optional<Reader> actual = testee.locate(classes, file);
    return content(actual);
  }


  private void createFile(Path file, String content) throws IOException {
    if (file.getParent() != null) {
      Files.createDirectories(file.getParent());
    }

    Files.write(file, content.getBytes(StandardCharsets.UTF_8));
  }

  private String content(Optional<Reader> reader) throws Exception {
    if (reader.isPresent()) {
      return content(reader.get());
    }
    return "";
  }

  private String content(Reader reader) throws Exception {
    String s = "";
    int ch;
    while ((ch = reader.read()) != -1) {
      s += (char) ch;
    }
    reader.close();
    return s;
  }
}
