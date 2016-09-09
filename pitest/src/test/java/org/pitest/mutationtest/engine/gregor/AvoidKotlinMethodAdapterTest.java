package org.pitest.mutationtest.engine.gregor;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.bytecode.MethodDecoratorTest;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;

public class AvoidKotlinMethodAdapterTest extends MethodDecoratorTest {
    @Mock
    private MethodMutationContext context;


    private AvoidKotlinMethodAdapter testee;

    @Override
    @Before
    public void setUp() {
        super.setUp();
        testee = new AvoidKotlinMethodAdapter(this.context, this.mv);
    }

    @Override
    protected MethodVisitor getTesteeVisitor() {
        return this.testee;
    }

    @Test
    public void shouldDisableMutationsForCallsToKotlinIntrinsics() {
        this.testee.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "kotlin/jvm/internal/Intrinsics",
                "anyMethodName", "(Ljava/lang/Object;Ljava/lang/String;)V", true);
        verify(this.context).disableMutations(anyString());
    }

}
