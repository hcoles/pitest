package org.pitest.mutationtest.engine.gregor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.pitest.bytecode.MethodDecoratorTest;

public class AvoidAnnotatedMethodsFilterTest extends MethodDecoratorTest {
  
  @Mock
  private MethodMutationContext     context;

  @Mock
  private Label                     label;

  @Mock
  private AnnotationVisitor av;
  
  private AvoidAnnotatedMethodsFilter testee;


  @Override
  @Before
  public void setUp() {
    super.setUp();
    this.testee = new AvoidAnnotatedMethodsFilter(this.context, this.mv);
  }

  @Test
  public void shouldProcessAnnotations() {
    when(mv.visitAnnotation(anyString(), anyBoolean())).thenReturn(av);
    assertThat(testee.visitAnnotation("any", true)).isSameAs(av);
  }

  @Test
  public void shouldDisableMutationsWhenGeneratedAnnotationPresent() {
    testee.visitAnnotation("com.example.Generated;", true);
    verify(context).disableMutations(anyString());
  } 
  
  @Test
  public void shouldNotDisableMutationsWhenUnknownAnnoations() {
    testee.visitAnnotation("foo", true);
    verify(context, never()).disableMutations(anyString());
  }
  
  @Test
  public void shouldRenableMutationAtEndOfMethod() {
    testee.visitEnd();
    verify(context).enableMutatations(anyString());
  }
  
  @Override
  protected MethodVisitor getTesteeVisitor() {
    return testee;
  }

}
