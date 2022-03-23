package org.pitest.mutationtest.engine.gregor.mutators;

import org.junit.Test;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.mutators.RemoveConditionalMutator.Choice;
import org.pitest.verifier.mutants.MutatorVerifierStart;

import java.util.function.Function;
import java.util.function.IntFunction;

import static org.junit.Assert.assertEquals;

public class RemoveConditionalMutatorTest {

    MutatorVerifierStart v;

    private static int getZeroButPreventInlining() {
        return 0;
    }

    @Test
    public void shouldProvideAMeaningfulName() {
        assertEquals("REMOVE_CONDITIONALS_EQUAL_IF",
                new RemoveConditionalMutator(Choice.EQUAL, true).getName());
        assertEquals("REMOVE_CONDITIONALS_EQUAL_ELSE",
                new RemoveConditionalMutator(Choice.EQUAL, false).getName());
        assertEquals("REMOVE_CONDITIONALS_ORDER_IF",
                new RemoveConditionalMutator(Choice.ORDER, true).getName());
        assertEquals("REMOVE_CONDITIONALS_ORDER_ELSE",
                new RemoveConditionalMutator(Choice.ORDER, false).getName());
    }

    @Test
    public void shouldReplaceIFEQ_EQUAL_T() {

        v = forMutator(new RemoveConditionalMutator(Choice.EQUAL, true));
        v.forIntFunctionClass(HasIFEQ.class)
                .firstMutantShouldReturn(1, "was not zero");
        v.forIntFunctionClass(HasIFEQ.class)
                .firstMutantShouldReturn(0, "was not zero");

    }

    @Test
    public void shouldDescribeReplacementOfEqualityChecksWithTrue() {
        v = forMutator(new RemoveConditionalMutator(Choice.EQUAL, true));
        v.forClass(HasIFEQ.class)
                .firstMutantDescription()
                .contains("equality check with true");
    }

    @Test
    public void shouldDescribeReplacementOfEqualityChecksWithFalse() {
        v = forMutator(new RemoveConditionalMutator(Choice.EQUAL, false));
        v.forClass(HasIFEQ.class)
                .firstMutantDescription().contains(
                        "equality check with false");
    }

    @Test
    public void shouldReplaceIFEQ_EQUAL_F() {
        v = forMutator(new RemoveConditionalMutator(Choice.EQUAL, false));
        v.forIntFunctionClass(HasIFEQ.class)
                .firstMutantShouldReturn(1, "was zero");
        v.forIntFunctionClass(HasIFEQ.class)
                .firstMutantShouldReturn(0, "was zero");
    }

    @Test
    public void shouldNotReplaceIFEQ_ORDER_T() {
        v = forMutator(new RemoveConditionalMutator(Choice.ORDER, true));
        v.forClass(HasIFEQ.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldNotReplaceIFEQ_ORDER_F() {
        v = forMutator(new RemoveConditionalMutator(Choice.ORDER, false));
        v.forClass(HasIFEQ.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldReplaceIFNE_EQUAL_T() {
        v = forMutator(new RemoveConditionalMutator(Choice.EQUAL, true));
        v.forIntFunctionClass(HasIFNE.class)
                .firstMutantShouldReturn(1, "was zero");
        v.forIntFunctionClass(HasIFNE.class)
                .firstMutantShouldReturn(0, "was zero");
    }

    @Test
    public void shouldReplaceIFNE_EQUAL_F() {
        v = forMutator(new RemoveConditionalMutator(Choice.EQUAL, false));
        v.forIntFunctionClass(HasIFNE.class)
                .firstMutantShouldReturn(1, "was not zero");
        v.forIntFunctionClass(HasIFNE.class)
                .firstMutantShouldReturn(0, "was not zero");
    }

    @Test
    public void shouldReplaceIFNE_ORDER_T() {
        v = forMutator(new RemoveConditionalMutator(Choice.ORDER, true));
        v.forClass(HasIFNE.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldReplaceIFNE_ORDER_F() {
        v = forMutator(new RemoveConditionalMutator(Choice.ORDER, false));
        v.forClass(HasIFNE.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldReplaceIFNULL_EQUAL_T() {
        v = forMutator(new RemoveConditionalMutator(Choice.EQUAL, true));
        v.forFunctionClass(HasIFNULL.class)
                .firstMutantShouldReturn(() -> null, "was not null");
        v.forFunctionClass(HasIFNULL.class)
                .firstMutantShouldReturn(() -> "foo", "was not null");
    }

    @Test
    public void shouldReplaceIFNULL_EQUAL_F() {
        v = forMutator(new RemoveConditionalMutator(Choice.EQUAL, false));
        v.forFunctionClass(HasIFNULL.class);
        v.forFunctionClass(HasIFNULL.class)
                .firstMutantShouldReturn(() -> null, "was null");
        v.forFunctionClass(HasIFNULL.class)
                .firstMutantShouldReturn(() -> "foo", "was null");
    }

    @Test
    public void shouldReplaceIFNULL_ORDER_T() {
        v = forMutator(new RemoveConditionalMutator(Choice.ORDER, true));
        v.forClass(HasIFNULL.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldReplaceIFNULL_ORDER_F() {
        v = forMutator(new RemoveConditionalMutator(Choice.ORDER, false));
        v.forClass(HasIFNULL.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldReplaceIFNONNULL_EQUAL_T() {
        v = forMutator(new RemoveConditionalMutator(Choice.EQUAL, true));
        v.forFunctionClass(HasIFNONNULL.class)
                .firstMutantShouldReturn(() -> null, "was null");
        v.forFunctionClass(HasIFNONNULL.class)
                .firstMutantShouldReturn("foo", "was null");
    }

    @Test
    public void shouldReplaceIFNONNULL_EQUAL_F() {
        v = forMutator(new RemoveConditionalMutator(Choice.EQUAL, false));
        v.forFunctionClass(HasIFNONNULL.class);
        v.forFunctionClass(HasIFNONNULL.class)
                .firstMutantShouldReturn(() -> null, "was not null");
        v.forFunctionClass(HasIFNONNULL.class)
                .firstMutantShouldReturn("foo", "was not null");
    }

    @Test
    public void shouldReplaceIFNONNULL_ORDER_T() {
        v = forMutator(new RemoveConditionalMutator(Choice.ORDER, true));
        v.forClass(HasIFNONNULL.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldReplaceIFNONNULL_ORDER_F() {
        v = forMutator(new RemoveConditionalMutator(Choice.ORDER, false));
        v.forClass(HasIFNONNULL.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldReplaceIF_ICMPNE_EQUAL_T() {
        v = forMutator(new RemoveConditionalMutator(Choice.EQUAL, true));
        v.forIntFunctionClass(HasIF_ICMPNE.class)
                .firstMutantShouldReturn(1, "was zero");
        v.forIntFunctionClass(HasIF_ICMPNE.class)
                .firstMutantShouldReturn(0, "was zero");

    }

    @Test
    public void shouldReplaceIF_ICMPNE_EQUAL_F() {
        v = forMutator(new RemoveConditionalMutator(Choice.EQUAL, false));
        v.forIntFunctionClass(HasIF_ICMPNE.class);
        v.forIntFunctionClass(HasIF_ICMPNE.class)
                .firstMutantShouldReturn(1, "was not zero");
        v.forIntFunctionClass(HasIF_ICMPNE.class)
                .firstMutantShouldReturn(0, "was not zero");
    }

    @Test
    public void shouldReplaceIF_ICMPNE_ORDER_T() {
        v = forMutator(new RemoveConditionalMutator(Choice.ORDER, true));
        v.forClass(HasIF_ICMPNE.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldReplaceIF_ICMPNE_ORDER_F() {
        v = forMutator(new RemoveConditionalMutator(Choice.ORDER, false));
        v.forClass(HasIF_ICMPNE.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldReplaceIF_ICMPEQ_EQUAL_T() {
        v = forMutator(new RemoveConditionalMutator(Choice.EQUAL, true));
        v.forIntFunctionClass(HasIF_ICMPEQ.class)
                .firstMutantShouldReturn(1, "was not zero");
        v.forIntFunctionClass(HasIF_ICMPEQ.class)
                .firstMutantShouldReturn(0, "was not zero");
    }

    @Test
    public void shouldReplaceIF_ICMPEQ_EQUAL_F() {
        v = forMutator(new RemoveConditionalMutator(Choice.EQUAL, false));
        v.forIntFunctionClass(HasIF_ICMPEQ.class);
        v.forIntFunctionClass(HasIF_ICMPEQ.class)
                .firstMutantShouldReturn(1, "was zero");
        v.forIntFunctionClass(HasIF_ICMPEQ.class)
                .firstMutantShouldReturn(0, "was zero");
    }

    @Test
    public void shouldReplaceIF_ICMPEQ_ORDER_T() {
        v = forMutator(new RemoveConditionalMutator(Choice.ORDER, true));
        v.forClass(HasIF_ICMPEQ.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldReplaceIF_ICMPEQ_ORDER_F() {
        v = forMutator(new RemoveConditionalMutator(Choice.ORDER, false));
        v.forClass(HasIF_ICMPEQ.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldReplaceIF_ACMPEQ_EQUAL_T() {
        v = forMutator(new RemoveConditionalMutator(Choice.EQUAL, true));
        v.forFunctionClass(HasIF_ACMPEQ.class)
                .firstMutantShouldReturn(1, "was not zero");
        v.forFunctionClass(HasIF_ACMPEQ.class)
                .firstMutantShouldReturn(0, "was not zero");
    }

    @Test
    public void shouldReplaceIF_ACMPEQ_EQUAL_F() {
        v = forMutator(new RemoveConditionalMutator(Choice.EQUAL, false));

        v.forFunctionClass(HasIF_ACMPEQ.class)
                .firstMutantShouldReturn(1, "was zero");
        v.forFunctionClass(HasIF_ACMPEQ.class)
                .firstMutantShouldReturn(0, "was zero");
    }

    @Test
    public void shouldReplaceIF_ACMPEQ_ORDER_T() {
        v = forMutator(new RemoveConditionalMutator(Choice.ORDER, true));

        v.forClass(HasIF_ACMPEQ.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldReplaceIF_ACMPEQ_ORDER_F() {
        v = forMutator(new RemoveConditionalMutator(Choice.ORDER, false));
        v.forClass(HasIF_ACMPEQ.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldReplaceIF_ACMPNE_EQUAL_T() {
        v = forMutator(new RemoveConditionalMutator(Choice.EQUAL, true));
        v.forFunctionClass(HasIF_ACMPNE.class)
                .firstMutantShouldReturn(1, "was not zero");
        v.forFunctionClass(HasIF_ACMPNE.class)
                .firstMutantShouldReturn(0, "was not zero");
    }

    @Test
    public void shouldReplaceIF_ACMPNE_EQUAL_F() {
        v = forMutator(new RemoveConditionalMutator(Choice.EQUAL, false));
        v.forFunctionClass(HasIF_ACMPNE.class)
                .firstMutantShouldReturn(1, "was zero");
        v.forFunctionClass(HasIF_ACMPNE.class)
                .firstMutantShouldReturn(0, "was zero");
    }

    @Test
    public void shouldReplaceIF_ACMPNE_ORDER_T() {
        v = forMutator(new RemoveConditionalMutator(Choice.ORDER, true));

        v.forClass(HasIF_ACMPNE.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldReplaceIF_ACMPNE_ORDER_F() {
        v = forMutator(new RemoveConditionalMutator(Choice.ORDER, false));
        v.forClass(HasIF_ACMPNE.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldNotReplaceIFLE_EQUAL_T() {
        v = forMutator(new RemoveConditionalMutator(Choice.EQUAL, true));

        v.forClass(HasIFLE.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldNotReplaceIFLE_EQUAL_F() {
        v = forMutator(new RemoveConditionalMutator(Choice.EQUAL, false));
        v.forClass(HasIFLE.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldReplaceIFLE_ORDER_T() {
        v = forMutator(new RemoveConditionalMutator(Choice.ORDER, true));
        v.forIntFunctionClass(HasIFLE.class)
                .firstMutantShouldReturn(1, "was > zero");
        v.forIntFunctionClass(HasIFLE.class)
                .firstMutantShouldReturn(0, "was > zero");
    }

    @Test
    public void shouldReplaceIFLE_ORDER_F() {
        v = forMutator(new RemoveConditionalMutator(Choice.ORDER, false));
        v.forIntFunctionClass(HasIFLE.class)
                .firstMutantShouldReturn(1, "was <= zero");
        v.forIntFunctionClass(HasIFLE.class)
                .firstMutantShouldReturn(0, "was <= zero");
    }

    @Test
    public void shouldDescribeReplacementOfOrderCheckWithTrue() {
        v = forMutator(new RemoveConditionalMutator(Choice.ORDER, true));
        v.forIntFunctionClass(HasIFLE.class)
                .firstMutantDescription()
                .contains(
                        " comparison check with true");
    }

    @Test
    public void shouldDescribeReplacementOfOrderCheckWithFalse() {
        v = forMutator(new RemoveConditionalMutator(Choice.ORDER, false));
        v.forIntFunctionClass(HasIFLE.class)
                .firstMutantDescription()
                .contains(
                        " comparison check with false");
    }

    @Test
    public void shouldNotReplaceIFGE_EQUAL_T() {
        v = forMutator(new RemoveConditionalMutator(Choice.EQUAL, true));

        v.forClass(HasIFGE.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldNotReplaceIFGE_EQUAL_F() {
        v = forMutator(new RemoveConditionalMutator(Choice.EQUAL, false));
        v.forClass(HasIFGE.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldReplaceIFGE_ORDER_T() {
        v = forMutator(new RemoveConditionalMutator(Choice.ORDER, true));
        v.forIntFunctionClass(HasIFGE.class)
                .firstMutantShouldReturn(1, "was < zero");
        v.forIntFunctionClass(HasIFGE.class)
                .firstMutantShouldReturn(0, "was < zero");
    }

    @Test
    public void shouldReplaceIFGE_ORDER_F() {
        v = forMutator(new RemoveConditionalMutator(Choice.ORDER, false));
        v.forIntFunctionClass(HasIFGE.class);
        final String expected = "was >= zero";
        v.forIntFunctionClass(HasIFGE.class)
                .firstMutantShouldReturn(1, expected);
        v.forIntFunctionClass(HasIFGE.class)
                .firstMutantShouldReturn(0, expected);
    }

    @Test
    public void shouldNotReplaceIFGT_EQUAL_T() {
        v = forMutator(new RemoveConditionalMutator(Choice.EQUAL, true));

        v.forClass(HasIFGT.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldNotReplaceIFGT_EQUAL_F() {
        v = forMutator(new RemoveConditionalMutator(Choice.EQUAL, false));
        v.forClass(HasIFGT.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldReplaceIFGT_ORDER_T() {
        final String expected = "was <= zero";
        v = forMutator(new RemoveConditionalMutator(Choice.ORDER, true));
        v.forIntFunctionClass(HasIFGT.class)
                .firstMutantShouldReturn(1, expected);
        v.forIntFunctionClass(HasIFGT.class)
                .firstMutantShouldReturn(0, expected);
    }

    @Test
    public void shouldReplaceIFGT_ORDER_F() {
        v = forMutator(new RemoveConditionalMutator(Choice.ORDER, false));

        final String expected = "was > zero";
        v.forIntFunctionClass(HasIFGT.class)
                .firstMutantShouldReturn(1, expected);
        v.forIntFunctionClass(HasIFGT.class)
                .firstMutantShouldReturn(0, expected);
    }

    @Test
    public void shouldNotReplaceIFLT_EQUAL_T() {
        v = forMutator(new RemoveConditionalMutator(Choice.EQUAL, true));

        v.forClass(HasIFLT.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldNotReplaceIFLT_EQUAL_F() {
        v = forMutator(new RemoveConditionalMutator(Choice.EQUAL, false));
        v.forClass(HasIFLT.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldReplaceIFLT_ORDER_T() {
        v = forMutator(new RemoveConditionalMutator(Choice.ORDER, true));
        final String expected = "was >= zero";
        v.forIntFunctionClass(HasIFLT.class)
                .firstMutantShouldReturn(1, expected);
        v.forIntFunctionClass(HasIFLT.class)
                .firstMutantShouldReturn(0, expected);
    }

    @Test
    public void shouldReplaceIFLT_ORDER_T_F() {
        v = forMutator(new RemoveConditionalMutator(Choice.ORDER, false));

        final String expected = "was < zero";
        v.forIntFunctionClass(HasIFLT.class)
                .firstMutantShouldReturn(1, expected);
        v.forIntFunctionClass(HasIFLT.class)
                .firstMutantShouldReturn(0, expected);
    }

    @Test
    public void shouldNotReplaceIF_ICMPLE_EQUAL_T() {
        v = forMutator(new RemoveConditionalMutator(Choice.EQUAL, true));

        v.forClass(HasIF_ICMPLE.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldNotReplaceIF_ICMPLE_EQUAL_F() {
        v = forMutator(new RemoveConditionalMutator(Choice.EQUAL, false));
        v.forClass(HasIF_ICMPLE.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldReplaceIF_ICMPLE_ORDER_T() {
        v = forMutator(new RemoveConditionalMutator(Choice.ORDER, true));
        final String expected = "was > zero";
        v.forIntFunctionClass(HasIF_ICMPLE.class)
                .firstMutantShouldReturn(1, expected);
        v.forIntFunctionClass(HasIF_ICMPLE.class)
                .firstMutantShouldReturn(0, expected);

    }

    @Test
    public void shouldReplaceIF_ICMPLE_ORDER_F() {
        v = forMutator(new RemoveConditionalMutator(Choice.ORDER, false));

        final String expected = "was <= zero";
        v.forIntFunctionClass(HasIF_ICMPLE.class)
                .firstMutantShouldReturn(1, expected);
        v.forIntFunctionClass(HasIF_ICMPLE.class)
                .firstMutantShouldReturn(0, expected);
    }

    @Test
    public void shouldNotReplaceIF_ICMPGE_EQUAL_T() {
        v = forMutator(new RemoveConditionalMutator(Choice.EQUAL, true));

        v.forClass(HasIF_ICMPGE.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldNotReplaceIF_ICMPGE_EQUAL_F() {
        v = forMutator(new RemoveConditionalMutator(Choice.EQUAL, false));
        v.forClass(HasIF_ICMPGE.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldReplaceIF_ICMPGE_ORDER_T() {
        v = forMutator(new RemoveConditionalMutator(Choice.ORDER, true));
        final String expected = "was < zero";
        v.forIntFunctionClass(HasIF_ICMPGE.class)
                .firstMutantShouldReturn(1, expected);
        v.forIntFunctionClass(HasIF_ICMPGE.class)
                .firstMutantShouldReturn(0, expected);

    }

    @Test
    public void shouldReplaceIF_ICMPGE_ORDER_F() {
        v = forMutator(new RemoveConditionalMutator(Choice.ORDER, false));

        final String expected = "was >= zero";
        v.forIntFunctionClass(HasIF_ICMPGE.class)
                .firstMutantShouldReturn(1, expected);
        v.forIntFunctionClass(HasIF_ICMPGE.class)
                .firstMutantShouldReturn(0, expected);
    }

    @Test
    public void shouldNotReplaceIF_ICMPGT_EQUAL_T() {
        v = forMutator(new RemoveConditionalMutator(Choice.EQUAL, true));

        v.forClass(HasIF_ICMPGT.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldNotReplaceIF_ICMPGT_EQUAL_F() {
        v = forMutator(new RemoveConditionalMutator(Choice.EQUAL, false));
        v.forClass(HasIF_ICMPGT.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldReplaceIF_ICMPGT_ORDER_T() {
        v = forMutator(new RemoveConditionalMutator(Choice.ORDER, true));
        final String expected = "was <= zero";
        v.forIntFunctionClass(HasIF_ICMPGT.class)
                .firstMutantShouldReturn(1, expected);
        v.forIntFunctionClass(HasIF_ICMPGT.class)
                .firstMutantShouldReturn(2, expected);

    }

    @Test
    public void shouldReplaceIF_ICMPGT_ORDER_F() {
        v = forMutator(new RemoveConditionalMutator(Choice.ORDER, false));

        final String expected = "was > zero";
        v.forIntFunctionClass(HasIF_ICMPGT.class)
                .firstMutantShouldReturn(1, expected);
        v.forIntFunctionClass(HasIF_ICMPGT.class)
                .firstMutantShouldReturn(2, expected);
    }

    @Test
    public void shouldNotReplaceIF_ICMPLT_EQUAL_T() {
        v = forMutator(new RemoveConditionalMutator(Choice.EQUAL, true));

        v.forClass(HasIF_ICMPLT.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldNotReplaceIF_ICMPLT_EQUAL_F() {
        v = forMutator(new RemoveConditionalMutator(Choice.EQUAL, false));
        v.forClass(HasIF_ICMPLT.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldReplaceIF_ICMPLT_ORDER_T() {
        v = forMutator(new RemoveConditionalMutator(Choice.ORDER, true));
        v.forIntFunctionClass(HasIF_ICMPLT.class)
                .firstMutantShouldReturn(1, "was >= zero");
        v.forIntFunctionClass(HasIF_ICMPLT.class)
                .firstMutantShouldReturn(0, "was >= zero");
    }

    @Test
    public void shouldReplaceIF_ICMPLT_ORDER_F() {
        v = forMutator(new RemoveConditionalMutator(Choice.ORDER, false));
        v.forIntFunctionClass(HasIF_ICMPLT.class);
        v.forIntFunctionClass(HasIF_ICMPLT.class)
                .firstMutantShouldReturn(1, "was < zero");
        v.forIntFunctionClass(HasIF_ICMPLT.class)
                .firstMutantShouldReturn(0, "was < zero");
    }

    private static class HasIFEQ implements IntFunction<String> {
        @Override
        public String apply(int i) {
            if (i != 0) {
                return "was not zero";
            } else {
                return "was zero";
            }
        }
    }

    private static class HasIFNE implements IntFunction<String> {

        @Override
        public String apply(int i) {
            if (i == 0) {
                return "was zero";
            } else {
                return "was not zero";
            }
        }
    }

    private static class HasIFNULL implements Function<Object, String> {
        @Override
        public String apply(Object i) {
            if (i != null) {
                return "was not null";
            } else {
                return "was null";
            }
        }
    }

    private static class HasIFNONNULL implements Function<Object, String> {
        @Override
        public String apply(Object i) {
            if (i == null) {
                return "was null";
            } else {
                return "was not null";
            }
        }
    }

    private static class HasIF_ICMPNE implements IntFunction<String> {
        @Override
        public String apply(int i) {
            final int j = getZeroButPreventInlining();
            if (i == j) {
                return "was zero";
            } else {
                return "was not zero";
            }
        }
    }

    private static class HasIF_ICMPEQ implements IntFunction<String> {
        @Override
        public String apply(int i) {
            final int j = getZeroButPreventInlining();
            if (i != j) {
                return "was not zero";
            } else {
                return "was zero";
            }
        }
    }

    static class HasIF_ACMPEQ implements Function<Object, String> {
        @Override
        public String apply(Object i) {
            if (i != this) {
                return "was not zero";
            } else {
                return "was zero";
            }
        }
    }

    static class HasIF_ACMPNE implements Function<Object, String> {
        @Override
        public String apply(Object i) {
            if (i == this) {
                return "was not zero";
            } else {
                return "was zero";
            }
        }
    }

    static class HasIFLE implements IntFunction<String> {
        @Override
        public String apply(int i) {
            if (i > 0) {
                return "was > zero";
            } else {
                return "was <= zero";
            }
        }
    }

    static class HasIFGE implements IntFunction<String> {
        @Override
        public String apply(int i) {
            if (i < 0) {
                return "was < zero";
            } else {
                return "was >= zero";
            }
        }
    }

    static class HasIFGT implements IntFunction<String> {
        @Override
        public String apply(int i) {
            if (i <= 0) {
                return "was <= zero";
            } else {
                return "was > zero";
            }
        }
    }

    static class HasIFLT implements IntFunction<String> {
        @Override
        public String apply(int i) {
            if (i >= 0) {
                return "was >= zero";
            } else {
                return "was < zero";
            }
        }
    }

    static class HasIF_ICMPLE implements IntFunction<String> {
        @Override
        public String apply(int i) {
            final int j = getZeroButPreventInlining();
            if (i > j) {
                return "was > zero";
            } else {
                return "was <= zero";
            }
        }
    }

    static class HasIF_ICMPGE implements IntFunction<String> {
        @Override
        public String apply(int i) {
            final int j = getZeroButPreventInlining();
            if (i < j) {
                return "was < zero";
            } else {
                return "was >= zero";
            }
        }
    }

    static class HasIF_ICMPGT implements IntFunction<String> {
        @Override
        public String apply(int i) {
            final int j = getZeroButPreventInlining();
            if (i <= j) {
                return "was <= zero";
            } else {
                return "was > zero";
            }
        }
    }

    static class HasIF_ICMPLT implements IntFunction<String> {
        @Override
        public String apply(int i) {
            final int j = getZeroButPreventInlining();
            if (i >= j) {
                return "was >= zero";
            } else {
                return "was < zero";
            }
        }
    }

    MutatorVerifierStart forMutator(MethodMutatorFactory m) {
      return MutatorVerifierStart.forMutator(m)
              .notCheckingUnMutatedValues();
    }
}
