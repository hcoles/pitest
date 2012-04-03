package org.pitest.mutationtest.engine.gregor;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class LineFilterMethodAdapterTest {

  @Mock
  private Context                 context;

  @Mock
  private MethodVisitor           child;

  @Mock
  private PremutationClassInfo    classInfo;

  @Mock
  private Label                   label;

  private LineFilterMethodAdapter testee;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    testee = new LineFilterMethodAdapter(context, classInfo, child);
  }

  @Test
  public void shouldDisableMutationsWhenEncountersExcludedLine() {
    when(classInfo.isLoggingLine(1)).thenReturn(true);
    this.testee.visitLineNumber(1, label);
    verify(this.context).disableMutations(anyString());
  }

  @Test
  public void shouldenableMutationsWhenEncountersANonExcludedLine() {
    when(classInfo.isLoggingLine(1)).thenReturn(false);
    this.testee.visitLineNumber(1, label);
    verify(this.context).enableMutatations(anyString());
  }

  @Test
  public void shouldForwardVisitLineNumberCallsToChild() {
    this.testee.visitLineNumber(0, label);
    verify(this.child).visitLineNumber(0, label);
  }

}
