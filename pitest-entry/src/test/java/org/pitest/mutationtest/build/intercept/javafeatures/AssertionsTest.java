package org.pitest.mutationtest.build.intercept.javafeatures;

import org.junit.Test;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.pitest.mutationtest.engine.gregor.mutators.NullMutateEverything;
import org.pitest.verifier.interceptors.InterceptorVerifier;
import org.pitest.verifier.interceptors.VerifierStart;

import java.util.function.Predicate;

import static org.pitest.bytecode.analysis.InstructionMatchers.isA;
import static org.pitest.bytecode.analysis.OpcodeMatchers.ATHROW;

public class AssertionsTest {
    AssertionsFilterFactory underTest = new AssertionsFilterFactory();
    InterceptorVerifier v = VerifierStart.forInterceptorFactory(underTest)
            .usingMutator(new NullMutateEverything());

    @Test
    public void doesNotFilterCodeWithNoAsserts() {
        v.forClass(NoAsserts.class)
                .forAnyCode()
                .mutantsAreGenerated()
                .noMutantsAreFiltered()
                .verify();
    }

    @Test
    public void filtersAssertCode() {
        v.forClass(HasAssertStatement.class)
                .forMethod("foo")
                .forCodeMatching(assertInstructions())
                .mutantsAreGenerated()
                .allMutantsAreFiltered()
                .verify();
    }

    @Test
    public void filtersAssertCodeWhenOtherStatementsPresent() {
        v.forClass(HasAssertStatementAndOtherStatements.class)
                .forMethod("foo")
                .forCodeMatching(sysoutFieldReference())
                .mutantsAreGenerated()
                .noMutantsAreFiltered()
                .verify();
    }

    @Test
    public void leavesCodeNotInAssert() {
        v.forClass(HasAssertStatementAndOtherStatements.class)
                .forMethod("foo")
                .forCodeMatching(sysoutFieldReference())
                .mutantsAreGenerated()
                .noMutantsAreFiltered()
                .verify();
    }

    @Test
    public void filtersMultipleAsserts() {
        v.forClass(MultipleAsserts.class)
                .forMethod("foo")
                .forCodeMatching(assertInstructions())
                .mutantsAreGenerated()
                .allMutantsAreFiltered()
                .verify();
    }

    @Test
    public void leavesCodeNotInAssertWhenMultipleAsserts() {
        v.forClass(MultipleAsserts.class)
                .forMethod("foo")
                .forCodeMatching(assertInstructions())
                .mutantsAreGenerated()
                .allMutantsAreFiltered()
                .verify();
    }

    private Predicate<AbstractInsnNode> assertInstructions() {
        return isA(JumpInsnNode.class).or(ATHROW).asPredicate().or(fieldAccess());
    }

    private Predicate<? super AbstractInsnNode> fieldAccess() {
        return isA(FieldInsnNode.class).asPredicate().and(sysoutFieldReference().negate());
    }

    private Predicate<AbstractInsnNode> sysoutFieldReference() {
        return n -> n instanceof FieldInsnNode && ((FieldInsnNode) n).owner.equals("java/lang/System");
    }

    static class HasAssertStatement {
        public void foo(final int i) {
            assert ((i + 20) > 10);
        }
    }

    static class HasAssertStatementAndOtherStatements {
        public int state;

        public void foo(final int i) {
            System.out.println("before");
            assert ((i + 20) > 10);
            System.out.println("after");
        }
    }

    static class NoAsserts {
        static boolean aField = true;
        public void foo(final int i) {
            if (aField) {
                throw new AssertionError();
            }
            if (i > 10) {
              throw new AssertionError();
            }
        }
    }

    static class MultipleAsserts {
        public int state;

        public void foo(final int i) {
            assert (i != 11);
            System.out.println("before");
            assert ((i + 20) > 10);
            System.out.println("middle");
            assert (i != 10);
            System.out.println("after");
        }
    }
}
