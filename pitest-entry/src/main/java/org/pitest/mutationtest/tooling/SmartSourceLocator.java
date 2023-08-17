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
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.pitest.mutationtest.SourceLocator;
import org.pitest.util.Unchecked;

public class SmartSourceLocator implements SourceLocator {

  private static final int                MAX_DEPTH = 4;

  private final List<SourceLocator> children;
  private final Charset inputCharset;

  public SmartSourceLocator(final Collection<Path> roots, Charset inputCharset) {
    this.inputCharset = inputCharset;
    final Collection<Path> childDirs = roots.stream()
                    .flatMap(r -> collectDirectories(r, MAX_DEPTH).stream())
                            .collect(Collectors.toList());

    childDirs.addAll(roots);

    this.children = childDirs.stream()
            .map(f -> new DirectorySourceLocator(f, this.inputCharset))
            .collect(Collectors.toList());
  }

  private Collection<Path> collectDirectories(Path root, int depth) {
    try {
      if (!Files.exists(root)) {
        return Collections.emptyList();
      }

      try (Stream<Path> matches = Files.find(root, depth, (unused, attributes) -> attributes.isDirectory())) {
        return matches.collect(Collectors.toList());
      }

    } catch (IOException ex) {
      throw Unchecked.translateCheckedException(ex);
    }

  }

  @Override
  public Optional<Reader> locate(Collection<String> classes, String fileName) {
    for (final SourceLocator each : this.children) {
      final Optional<Reader> reader = each.locate(classes, fileName);
      if (reader.isPresent()) {
        return reader;
      }
    }
    return Optional.empty();
  }

  /** Provide hint as to where to look when dealing with multiple modules. NOT thread safe.
   */
  public void sourceRootHint(Path file) {
    children.sort(new PathComparator(file, File.separator));
  }
}
