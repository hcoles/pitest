package tests;

import org.junit.Test;
import sources.MessageBuilder;

import static org.junit.Assert.assertEquals;

public class TestMessageBuilder {

    @Test
    public void testName() {
        MessageBuilder obj = new MessageBuilder();
        assertEquals("Hello there", obj.getMessage("there"));
    }

}
