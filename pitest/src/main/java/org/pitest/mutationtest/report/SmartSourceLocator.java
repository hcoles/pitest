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
package org.pitest.mutationtest.report;

import java.io.File;
import java.io.Reader;
import java.util.Collection;

import org.pitest.functional.F;
import org.pitest.functional.FArray;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;

public class SmartSourceLocator implements SourceLocator {

  private final Collection<SourceLocator> children;

  public SmartSourceLocator(final Collection<File> roots) {
    final F<File, Iterable<File>> collectChildren = new F<File, Iterable<File>>() {
      public Iterable<File> apply(final File a) {
        return collectNextTwoLevelOfDirectories(a);
      }
    };
    final Collection<File> childDirs = FCollection.flatMap(roots,
        collectChildren);
    childDirs.addAll(roots);

    final F<File, SourceLocator> fileToSourceLocator = new F<File, SourceLocator>() {
      public SourceLocator apply(final File a) {
        return new DirectorySourceLocator(a);
      }
    };
    this.children = FCollection.map(childDirs, fileToSourceLocator);
  }

  private Collection<File> collectNextTwoLevelOfDirectories(final File root) {
    final Collection<File> childDirs = listFirstLevelDirectories(root);
    final F<File, Iterable<File>> f = new F<File, Iterable<File>>() {
      public Iterable<File> apply(final File a) {
        return listFirstLevelDirectories(a);
      }
    };
    final Collection<File> secondLevelDirs = FCollection.flatMap(childDirs, f);
    childDirs.addAll(secondLevelDirs);
    return childDirs;
  }

  private static Collection<File> listFirstLevelDirectories(final File root) {
    final F<File, Boolean> p = new F<File, Boolean>() {
      public Boolean apply(final File a) {
        return a.isDirectory();
      }

    };
    return FArray.filter(root.listFiles(), p);
  }

  public Option<Reader> locate(final Collection<String> classes,
      final String fileName) {
    for (final SourceLocator each : this.children) {
      final Option<Reader> reader = each.locate(classes, fileName);
      if (reader.hasSome()) {
        return reader;
      }
    }
    return Option.none();
  }

}
