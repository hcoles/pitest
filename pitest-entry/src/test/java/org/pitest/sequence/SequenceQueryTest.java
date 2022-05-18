package org.pitest.sequence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.pitest.sequence.QueryStart.match;
import static org.pitest.sequence.Result.result;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

public class SequenceQueryTest {

    private static List<Integer> asList(Integer... is) {
        return Arrays.asList(is);
    }

    @Test
    public void shouldMatchSingleLiterals() {
        final SequenceMatcher<Integer> testee = match(eq(1))
                .compile();

        assertTrue(testee.matches(asList(1)));
        assertFalse(testee.matches(asList(2)));
    }

    @Test
    public void shouldMatchSimpleSequences() {
        final SequenceMatcher<Integer> testee = match(eq(1))
                .then(eq(2))
                .then(eq(3))
                .compile();

        assertTrue(testee.matches(asList(1, 2, 3)));
        assertFalse(testee.matches(asList(1, 2)));
        assertFalse(testee.matches(asList(1, 2, 3, 4)));
    }

    @Test
    public void shouldMatchSimpleOrs() {
        final SequenceQuery<Integer> right = match(eq(2));

        final SequenceMatcher<Integer> testee = match(eq(1))
                .or(right)
                .compile();

        assertTrue(testee.matches(asList(1)));
        assertTrue(testee.matches(asList(2)));
        assertFalse(testee.matches(asList(3)));
    }

    @Test
    public void shouldMatchSimpleZeroOrMores() {
        final SequenceQuery<Integer> right = match(eq(2));

        final SequenceMatcher<Integer> testee = match(eq(1))
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
        final SequenceQuery<Integer> right = match(eq(2));

        final SequenceMatcher<Integer> testee = match(eq(1))
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
        final SequenceQuery<Integer> left = match(eq(2));

        final SequenceQuery<Integer> right = match(eq(3));

        final SequenceMatcher<Integer> testee = match(eq(1))
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
        final SequenceMatcher<Integer> testee = match(eq(1))
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

        final SequenceMatcher<Integer> testee = match(eq(1))
                .then(matchesSlot(slot1.read()).and(write(slot2.write())))
                .then(matchesSlot(slot2.read()))
                .then(eq(4))
                .compile(QueryParams.params(Integer.class));

        assertTrue(testee.matches(sequence, context));
    }

    @Test
    public void returnsSingleMatchingContexts() {
        Slot<Integer> slot1 = Slot.create(Integer.class);

        List<Integer> sequence = asList(1, 2, 3);

        Context context = Context.start();

        final SequenceMatcher<Integer> testee = match(eq(1))
                .then(eq(2).and(write(slot1.write())))
                .then(eq(3))
                .compile(QueryParams.params(Integer.class));

        List<Context> actual = testee.contextMatches(sequence, context);
        assertThat(actual).hasSize(1);
        assertThat(actual.get(0).retrieve(slot1.read())).contains(2);
    }

    @Test
    public void returnsMultipleMatchingContexts() {
        Slot<Integer> slot1 = Slot.create(Integer.class);

        List<Integer> sequence = asList(1, 2, 3);

        Context context = Context.start();

        SequenceQuery<Integer> a = match(anyThing().and(write(slot1.write())))
                .zeroOrMore(match(anyThing()));
        SequenceQuery<Integer> b = match(eq(1))
                .then(anyThing().and(write(slot1.write())))
                .zeroOrMore(match(anyThing()));

        final SequenceMatcher<Integer> testee =
                a.or(b)
                .compile(QueryParams.params(Integer.class));

        List<Integer> actual = testee.contextMatches(sequence, context)
                        .stream().map(c -> c.retrieve(slot1.read()).get())
                .collect(Collectors.toList());

        assertThat(actual).hasSize(2);
        assertThat(actual).containsExactlyInAnyOrder(1,2);
    }

    private Match<Integer> anyThing() {
        return (c,i) -> result(true,c);
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
