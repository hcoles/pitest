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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.pitest.functional.Streams;
import org.pitest.mutationtest.SourceLocator;

public class DirectorySourceLocator implements SourceLocator {

  private final File                    root;
  private final Function<File, Optional<Reader>> fileToReader;

  private static class FileToReader implements Function<File, Optional<Reader>> {

    @Override
    public Optional<Reader> apply(final File f) {
      if (f.exists()) {
        try {
          return Optional.<Reader> ofNullable(new FileReader(f));
        } catch (final FileNotFoundException e) {
          return Optional.empty();
        }
      }
      return Optional.empty();
    }

  };

  DirectorySourceLocator(final File root,
      final Function<File, Optional<Reader>> fileToReader) {
    this.root = root;
    this.fileToReader = fileToReader;
  }

  public DirectorySourceLocator(final File root) {
    this(root, new FileToReader());
  }

  @Override
  public Optional<Reader> locate(final Collection<String> classes,
      final String fileName) {
    final Stream<Reader> matches = classes.stream().flatMap(classNameToSourceFileReader(fileName));
    return matches.findFirst();
  }

  private Function<String, Stream<Reader>> classNameToSourceFileReader(
      final String fileName) {
    return className -> {
      if (className.contains(".")) {
        final File f = new File(className.replace(".", File.separator));
        return locate(f.getParent() + File.separator + fileName);
      } else {
        return locate(fileName);
      }
    };
  }

  private Stream<Reader> locate(final String fileName) {
    final File f = new File(this.root + File.separator + fileName);
    return Streams.fromOptional(this.fileToReader.apply(f));
  }

}
