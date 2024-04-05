package org.pitest.mutationtest.verify;

/*
 * Copyright 2012 Henry Coles
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

import org.objectweb.asm.tree.LineNumberNode;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.classpath.CodeSource;
import org.pitest.help.Help;
import org.pitest.help.PitHelpError;

import java.util.Collections;
import java.util.List;

public class DefaultBuildVerifier implements BuildVerifier {

  private final CodeSource code;

  public DefaultBuildVerifier(CodeSource code) {
    this.code = code;
  }

  @Override
  public List<BuildMessage> verifyBuild() {

    // check we have at least one class that is not an interface
    // otherwise our checks will fire on an empty project
    boolean hasMutableCode = code.codeTrees()
            .anyMatch(this::isMutable);

    if (!hasMutableCode) {
      return Collections.emptyList();
    }

    checkForLineNumbers();

    checkForDebugSourceFile();

    return Collections.emptyList();
  }

  private void checkForDebugSourceFile() {
    // perform only a weak "any exist" check for source file as
    // some jvm languages are not guaranteed to include a source file for all classes
    boolean sourceFile = code.codeTrees()
            .anyMatch(this::hasSourceFile);

    if (!sourceFile) {
      throw new PitHelpError(Help.NO_SOURCE_FILE, code.codeTrees().findFirst().get().name().asJavaName());
    }
  }

  private void checkForLineNumbers() {
    // perform only a weak "any exist" check for line numbers as
    // some jvm languages are not guaranteed to produce them for all classes
    boolean lineNumbers = code.codeTrees()
            .anyMatch(this::hasLineNumbers);
    if (!lineNumbers) {
      throw new PitHelpError(Help.NO_LINE_NUMBERS);
    }
  }

  private boolean isMutable(ClassTree classTree) {
    return !classTree.isInterface() && !classTree.isSynthetic();
  }

  private boolean hasLineNumbers(ClassTree classTree) {
    return classTree.methods().stream()
            .anyMatch(m -> m.instructions().stream().anyMatch(n -> n instanceof LineNumberNode));
  }

  private boolean hasSourceFile(ClassTree classTree) {
    return classTree.rawNode().sourceFile != null;
  }

}
