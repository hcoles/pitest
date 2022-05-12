package org.pitest.sequence;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.pitest.sequence.Result.result;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class SequenceQueryTest {

    private static List<Integer> asList(Integer... is) {
        return Arrays.asList(is);
    }

    @Test
    public void shouldMatchSingleLiterals() {
        final SequenceMatcher<Integer> testee = QueryStart
                .match(eq(1))
                .compile();

        assertTrue(testee.matches(asList(1)));
        assertFalse(testee.matches(asList(2)));
    }

    @Test
    public void shouldMatchSimpleSequences() {
        final SequenceMatcher<Integer> testee = QueryStart
                .match(eq(1))
                .then(eq(2))
                .then(eq(3))
                .compile();

        assertTrue(testee.matches(asList(1, 2, 3)));
        assertFalse(testee.matches(asList(1, 2)));
        assertFalse(testee.matches(asList(1, 2, 3, 4)));
    }

    @Test
    public void shouldMatchSimpleOrs() {
        final SequenceQuery<Integer> right = QueryStart.match(eq(2));

        final SequenceMatcher<Integer> testee = QueryStart
                .match(eq(1))
                .or(right)
                .compile();

        assertTrue(testee.matches(asList(1)));
        assertTrue(testee.matches(asList(2)));
        assertFalse(testee.matches(asList(3)));
    }

    @Test
    public void shouldMatchSimpleZeroOrMores() {
        final SequenceQuery<Integer> right = QueryStart.match(eq(2));

        final SequenceMatcher<Integer> testee = QueryStart
                .match(eq(1))
                .zeroOrMore(right)
                .compile();

        assertTrue(testee.matches(asList(1)));
        assertTrue(testee.matches(asList(1, 2)));
        assertTrue(testee.matches(asList(1, 2, 2, 2)));
        assertFalse(testee.matches(asList(1, 2, 3)));
        assertFalse(testee.matches(asList(1, 3)));
    }

    @Test
    public void shouldMatchSimpleOneOrMores() {
        final SequenceQuery<Integer> right = QueryStart.match(eq(2));

        final SequenceMatcher<Integer> testee = QueryStart
                .match(eq(1))
                .oneOrMore(right)
                .compile();

        assertFalse(testee.matches(asList(1)));
        assertTrue(testee.matches(asList(1, 2)));
        assertTrue(testee.matches(asList(1, 2, 2, 2)));
        assertFalse(testee.matches(asList(1, 2, 3)));
        assertFalse(testee.matches(asList(1, 3)));
    }

    @Test
    public void shouldMatchAnyOf() {
        final SequenceQuery<Integer> left = QueryStart.match(eq(2));

        final SequenceQuery<Integer> right = QueryStart.match(eq(3));

        final SequenceMatcher<Integer> testee = QueryStart.match(eq(1))
                .thenAnyOf(left, right)
                .then(eq(99))
                .compile();

        assertTrue(testee.matches(asList(1, 2, 99)));
        assertTrue(testee.matches(asList(1, 3, 99)));
        assertFalse(testee.matches(asList(1, 2)));
        assertFalse(testee.matches(asList(1, 2, 3, 99)));
    }

    @Test
    public void shouldSkipItemsMatchingIgnoreList() {
        final SequenceMatcher<Integer> testee = QueryStart
                .match(eq(1))
                .then(eq(2))
                .compile(QueryParams.params(Integer.class).withIgnores(eq(99)));

        assertTrue(testee.matches(asList(1, 99, 2)));
    }

    @Test
    public void contextBranchesWithAnd() {
        Slot<Integer> slot1 = Slot.create(Integer.class);
        Slot<Integer> slot2 = Slot.create(Integer.class);

        List<Integer> sequence = asList(1, 2, 2, 4);

        Context context = Context.start()
                .store(slot1.write(), 2);

        final SequenceMatcher<Integer> testee = QueryStart
                .match(eq(1))
                .then(matchesSlot(slot1.read()).and(write(slot2.write())))
                .then(matchesSlot(slot2.read()))
                .then(eq(4))
                .compile(QueryParams.params(Integer.class));

        assertTrue(testee.matches(sequence, context));
    }

    private Match<Integer> write(SlotWrite<Integer> slot) {
        return (c, i) ->  result(true, c.store(slot, i));
    }

    private Match<Integer> matchesSlot(SlotRead<Integer> read) {
        return (c, i) -> {
            boolean b = c.retrieve(read).get().equals(i);
            return result(b,c);
        };
    }

    private Match<Integer> eq(final int i) {
        return Match.isEqual(i);
    }

}
