package org.pitest.sequence;

import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ContextTest {

    @Test
    public void retrieveIsStartsEmpty() {
        SlotRead<Integer> slot = Slot.create(Integer.class).read();
        assertThat(Context.start().retrieve(slot)).isEmpty();
    }

    @Test
    public void canStoreThenRetrieve() {
        SlotRead<Integer> slot = Slot.create(Integer.class).read();
        Context underTest = Context.start().store(slot.slot().write(), 42);

        Optional<Integer> actual = underTest.retrieve(slot);
        assertThat(actual).contains(42);
    }

    @Test
    public void canStoreAndRetrieveTwoValues() {
        Slot<Integer> slot1 = Slot.create(Integer.class);
        Slot<Integer> slot2 = Slot.create(Integer.class);
        Context underTest = Context.start()
                .store(slot1.write(), 42)
                .store(slot2.write(), 101);

        assertThat(underTest.retrieve(slot1.read())).contains(42);
        assertThat(underTest.retrieve(slot2.read())).contains(101);
    }

    @Test
    public void canStoreAndRetrieveThreeValues() {
        Slot<Integer> slot1 = Slot.create(Integer.class);
        Slot<Integer> slot2 = Slot.create(Integer.class);
        Slot<Integer> slot3 = Slot.create(Integer.class);
        Context underTest = Context.start()
                .store(slot1.write(), 42)
                .store(slot2.write(), 101)
                .store(slot3.write(), 8);

        assertThat(underTest.retrieve(slot1.read())).contains(42);
        assertThat(underTest.retrieve(slot2.read())).contains(101);
        assertThat(underTest.retrieve(slot3.read())).contains(8);
    }

    @Test
    public void canStoreAndRetrieveMultipleValues() {
        Slot<Integer> slot1 = Slot.create(Integer.class);
        Slot<Integer> slot2 = Slot.create(Integer.class);
        Slot<Integer> slot3 = Slot.create(Integer.class);
        Slot<Integer> slot4 = Slot.create(Integer.class);
        Slot<Integer> slot5 = Slot.create(Integer.class);
        Context underTest = Context.start()
                .store(slot1.write(), 1)
                .store(slot2.write(), 2)
                .store(slot3.write(), 3)
                .store(slot4.write(), 4)
                .store(slot5.write(), 5);


        assertThat(underTest.retrieve(slot1.read())).contains(1);
        assertThat(underTest.retrieve(slot2.read())).contains(2);
        assertThat(underTest.retrieve(slot3.read())).contains(3);
        assertThat(underTest.retrieve(slot4.read())).contains(4);
        assertThat(underTest.retrieve(slot5.read())).contains(5);
    }


}