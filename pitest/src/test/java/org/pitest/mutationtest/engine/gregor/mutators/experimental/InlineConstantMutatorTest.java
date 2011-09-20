/*
 * Copyright 2011 Henry Coles and Stefan Penndorf
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
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.pitest.mutationtest.engine.gregor.mutators.experimental;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.pitest.mutationtest.engine.gregor.Context;
import org.pitest.mutationtest.engine.gregor.MethodInfo;

/**
 * 
 * 
 * @author Stefan Penndorf <stefan.penndorf@gmail.com>
 */
public class InlineConstantMutatorTest {

  private static final Context           IGNORED_CONTEXT          = null;
  private static final MethodInfo        IGNORED_INFO             = null;
  private static final MethodVisitor     IGNORED_DELEGATE_VISITOR = mock(MethodVisitor.class);
  private InlineConstantMutator mutator;

  @Before
  public void setupMutator() {
    this.mutator = new InlineConstantMutator();
  }

  @Test
  public void shouldReturnUniqueId() {
    assertNotNull("Globally unique id may not be null",
        this.mutator.getGloballyUniqueId());
    assertFalse("Globally unique id may not be empty", this.mutator
        .getGloballyUniqueId().isEmpty());
  }

  @Test
  public void shouldCreateNonNullVisitor() {
    MethodVisitor visitor = mutator.create(IGNORED_CONTEXT, IGNORED_INFO,
        IGNORED_DELEGATE_VISITOR);
    assertNotNull(visitor);
  }
  
  @Test
  public void shouldCreateLineTrackingVisitor() {
    final int CURRENT_LINE = 25;
    final Label IGNORED_LABEL = null;
    final Context contextMock = mock(Context.class);
    
    final MethodVisitor visitor = mutator.create(contextMock, IGNORED_INFO,
        IGNORED_DELEGATE_VISITOR);
    
    visitor.visitLineNumber(CURRENT_LINE, IGNORED_LABEL);
    
    verify(contextMock).registerCurrentLine(CURRENT_LINE);
  }

}
