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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Collection;
import java.util.List;

import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;

public class DirectorySourceLocator implements SourceLocator {

  private final File root;

  public DirectorySourceLocator(final File root) {
    this.root = root;
  }

  public Option<Reader> locate(final Collection<String> classes,
      final String fileName) {
    final F<String, Iterable<Reader>> f = new F<String, Iterable<Reader>>() {

      public Iterable<Reader> apply(final String a) {
        final File f = new File(a.replace(".", File.separator));
        return locate(f.getParent() + File.separator + fileName);
      }

    };
    final List<Reader> matches = FCollection.flatMap(classes, f);
    if (matches.isEmpty()) {
      return Option.none();
    } else {
      return Option.some(matches.iterator().next());
    }
  }

  private final Option<Reader> locate(final String fileName) {
    final File f = new File(this.root + File.separator + fileName);
    if (f.exists()) {
      try {
        return Option.<Reader> some(new FileReader(f));
      } catch (final FileNotFoundException e) {
        return Option.none();
      }
    }
    return Option.none();
  }

}
