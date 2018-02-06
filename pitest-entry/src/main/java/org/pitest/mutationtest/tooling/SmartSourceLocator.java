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
import java.io.Reader;
import java.util.Collection;
import java.util.function.Function;

import org.pitest.functional.FArray;
import org.pitest.functional.FCollection;
import java.util.Optional;
import org.pitest.mutationtest.SourceLocator;

public class SmartSourceLocator implements SourceLocator {

  private static final int                MAX_DEPTH = 4;

  private final Collection<SourceLocator> children;

  public SmartSourceLocator(final Collection<File> roots) {
    final Collection<File> childDirs = FCollection.flatMap(roots,
        collectChildren(0));
    childDirs.addAll(roots);

    final Function<File, SourceLocator> fileToSourceLocator = a -> new DirectorySourceLocator(a);
    this.children = FCollection.map(childDirs, fileToSourceLocator);
  }

  private Function<File, Collection<File>> collectChildren(final int depth) {
    return a -> collectDirectories(a, depth);
  }

  private Collection<File> collectDirectories(final File root, final int depth) {
    final Collection<File> childDirs = listFirstLevelDirectories(root);
    if (depth < MAX_DEPTH) {
      childDirs.addAll(FCollection.flatMap(childDirs,
          collectChildren(depth + 1)));
    }
    return childDirs;

  }

  private static Collection<File> listFirstLevelDirectories(final File root) {
    return FArray.filter(root.listFiles(), a -> a.isDirectory());
  }

  @Override
  public Optional<Reader> locate(final Collection<String> classes,
      final String fileName) {
    for (final SourceLocator each : this.children) {
      final Optional<Reader> reader = each.locate(classes, fileName);
      if (reader.isPresent()) {
        return reader;
      }
    }
    return Optional.empty();
  }

}
