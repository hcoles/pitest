/*
 * Based on http://code.google.com/p/javacoveragent/ by
 * "alex.mq0" and "dmitry.kandalov"
 * 
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

package org.pitest.coverage.codeassist;

import org.objectweb.asm.AnnotationVisitor;

/**
 * @author ivanalx
 * @date 26.01.2009 15:48:51
 */
public class CoverageAnnotationVisitor implements AnnotationVisitor { // TODO do
  // we need
  // it?
  public void visit(final String name, final Object value) {

  }

  public void visitEnum(final String name, final String desc, final String value) {

  }

  public AnnotationVisitor visitAnnotation(final String name, final String desc) {
    return null;
  }

  public AnnotationVisitor visitArray(final String name) {
    return null;
  }

  public void visitEnd() {

  }
}
