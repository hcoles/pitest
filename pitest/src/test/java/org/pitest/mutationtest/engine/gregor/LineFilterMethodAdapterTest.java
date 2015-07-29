package org.pitest.mutationtest.engine.gregor;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.pitest.bytecode.MethodDecoratorTest;

public class LineFilterMethodAdapterTest extends MethodDecoratorTest {

  @Mock
  private MethodMutationContext   context;

  @Mock
  private PremutationClassInfo    classInfo;

  @Mock
  private Label                   label;

  private LineFilterMethodAdapter testee;

  @Override
  @Before
  public void setUp() {
    super.setUp();
    this.testee = new LineFilterMethodAdapter(this.context, this.classInfo,
        this.mv);
  }

  @Test
  public void shouldDisableMutationsWhenEncountersExcludedLine() {
    when(this.classInfo.isLineToAvoid(1)).thenReturn(true);
    this.testee.visitLineNumber(1, this.label);
    verify(this.context).disableMutations(anyString());
  }

  @Test
  public void shouldEnableMutationsWhenEncountersANonExcludedLine() {
    when(this.classInfo.isLineToAvoid(1)).thenReturn(false);
    this.testee.visitLineNumber(1, this.label);
    verify(this.context).enableMutatations(anyString());
  }

  @Test
  public void shouldForwardVisitLineNumberCallsToChild() {
    this.testee.visitLineNumber(0, this.label);
    verify(this.mv).visitLineNumber(0, this.label);
  }

  @Override
  protected MethodVisitor getTesteeVisitor() {
    return this.testee;
  }

}
