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

import java.util.Collection;

import org.pitest.classinfo.ClassInfo;
import org.pitest.functional.FCollection;
import org.pitest.functional.SideEffect1;
import org.pitest.help.Help;
import org.pitest.help.PitHelpError;
import org.pitest.mutationtest.CoverageDatabase;

public class DefaultBuildVerifier implements BuildVerifier {

  public void verify(final CoverageDatabase coverageDatabase) {
    final Collection<ClassInfo> codeClasses = coverageDatabase.getCodeClasses();
    FCollection.forEach(codeClasses, throwErrorIfHasNoLineNumbers());
    FCollection.forEach(codeClasses, throwErrorIfHasNoSourceFile());
  }

  private SideEffect1<ClassInfo> throwErrorIfHasNoLineNumbers() {
    return new SideEffect1<ClassInfo>() {
      public void apply(final ClassInfo a) {
        // ignore non top level classes - the compiler sometimes will generate
        // empty anonymous inner classes with no line numbers
        if (!a.isInterface() && a.isTopLevelClass() && (a.getNumberOfCodeLines() == 0)) {
          throw new PitHelpError(Help.NO_LINE_NUMBERS, a.getName().asJavaName());
        }
      }
    };
  }

  private SideEffect1<ClassInfo> throwErrorIfHasNoSourceFile() {
    return new SideEffect1<ClassInfo>() {
      public void apply(final ClassInfo a) {
        if (a.getSourceFileName() == null) {
          throw new PitHelpError(Help.NO_SOURCE_FILE, a.getName().asJavaName());
        }
      }
    };
  }

}
