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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.SourceLocator;
import org.pitest.util.Unchecked;

public class DirectorySourceLocator implements SourceLocator {

    private final Path root;
    private final Charset inputCharset;

    public DirectorySourceLocator(Path root, Charset inputCharset) {
        this.root = root;
        this.inputCharset = inputCharset;
    }

    @Override
    public Optional<Reader> locate(Collection<String> classes, String fileName) {

        if (!Files.exists(root)) {
            return Optional.empty();
        }

        // look for matching filename in directories matching its package.
        Optional<Path> path = classes.stream()
                .map(ClassName::fromString)
                .map(ClassName::getPackage)
                .distinct()
                .map(c -> toFileName(c, fileName))
                .map(file -> root.resolve(file))
                .filter(Files::exists)
                .filter(Files::isRegularFile)
                .findFirst();

        // If there is no file in the expected location (kotlin file?), search from the root, but
        // in this case we cannot know if we have the right file if the same name occurs more than once in the file tree
        // (cannot do this as an or as only introduced in java 9)
        if (path.isPresent()) {
            return path
                    .map(this::toReader);
        } else {
            return searchFromRoot(fileName)
                    .map(this::toReader);
        }
    }

    @Override
    // String value of path is used to select order to check
    // roots when locating files across multiple modules
    public String toString() {
        return root.toString();
    }

    private String toFileName(ClassName packge, String fileName) {
        if (packge.asJavaName().equals("")) {
            return fileName;
        }
        return packge.asJavaName().replace(".", File.separator) + File.separator + fileName;
    }

    private Reader toReader(Path path) {
        try {
            return new InputStreamReader(new BufferedInputStream(Files.newInputStream(path)),
                    inputCharset);
        } catch (IOException e) {
            throw Unchecked.translateCheckedException(e);
        }
    }

    private Optional<Path> searchFromRoot(String fileName) {
        try {
            try (Stream<Path> matches = Files.find(root, 100,
                            (path, attributes) -> path.getFileName().toString().equals(fileName) && attributes.isRegularFile())) {
                return matches.findFirst();
            }

        } catch (IOException e) {
            throw Unchecked.translateCheckedException(e);
        }
    }

}
