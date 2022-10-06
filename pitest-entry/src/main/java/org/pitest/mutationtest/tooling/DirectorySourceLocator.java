/*
 * Copyright 2010 Henry Coles
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.pitest.classinfo.ClassName;
import org.pitest.functional.Streams;
import org.pitest.mutationtest.SourceLocator;
import org.pitest.util.Unchecked;

public class DirectorySourceLocator implements SourceLocator {

  private final Path root;
  private final Function<Path, Optional<Reader>> fileToReader;

  private static final class FileToReader implements Function<Path, Optional<Reader>> {

    private final Charset inputCharset;

    private FileToReader(Charset inputCharset) {
      this.inputCharset = inputCharset;
    }

    @Override
    public Optional<Reader> apply(final Path f) {
      if (Files.exists(f)) {
        try {
          return Optional.of(new InputStreamReader(new BufferedInputStream(Files.newInputStream(f)),
                  inputCharset));
        } catch (final FileNotFoundException e) {
          return Optional.empty();
        } catch (IOException ex) {
          throw Unchecked.translateCheckedException(ex);
        }
      }
      return Optional.empty();
    }

  }

  DirectorySourceLocator(Path root, Function<Path, Optional<Reader>> fileToReader) {
    this.root = root;
    this.fileToReader = fileToReader;
  }

  public DirectorySourceLocator(Path root, Charset inputCharset) {
    this(root, new FileToReader(inputCharset));
  }

  @Override
  public Optional<Reader> locate(Collection<String> classes, String fileName) {
    final Stream<Reader> matches = classes.stream().flatMap(classNameToSourceFileReader(fileName));
    return matches.findFirst();
  }

  private Function<String, Stream<Reader>> classNameToSourceFileReader(
      final String fileName) {
    return className -> {
      if (className.contains(".")) {
        ClassName classPackage = ClassName.fromString(className).getPackage();
        String path = classPackage.asJavaName().replace(".", File.separator);
        return locate(path + File.separator + fileName);
      } else {
        return locate(fileName);
      }
    };
  }

  private Stream<Reader> locate(final String fileName) {
    return Streams.fromOptional(this.fileToReader.apply(root.resolve(fileName)));
  }

}
