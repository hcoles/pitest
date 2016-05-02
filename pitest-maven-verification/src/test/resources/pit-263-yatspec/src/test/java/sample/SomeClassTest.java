package sample;

import com.googlecode.yatspec.junit.Row;
import com.googlecode.yatspec.junit.Table;
import com.googlecode.yatspec.junit.TableRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(TableRunner.class)
public class SomeClassTest {

    @Table({
        @Row({"life the universe and everything", "42"}),
        @Row({"everything else", "-1"})
    })
    @Test
    public void testTheAnswer(String question, String answer) {
        assertEquals(new SomeClass().theAnswer(question), Integer.parseInt(answer));
    }
}