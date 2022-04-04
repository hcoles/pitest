package org.pitest.mutationtest.commandline;

import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.pitest.mutationtest.config.ConfigOption;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@RunWith(Parameterized.class)
public class CommaAwareArgsProcessorTest {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {  Arrays.asList("--unrelated", "a"), Collections.emptyList() },
                {  Arrays.asList("--jvmArgs", "a"), Arrays.asList("a") },
                {  Arrays.asList("--jvmArgs", "a,b,c,d"), Arrays.asList("a","b","c","d") },
                {  Arrays.asList("--jvmArgs", "a;b,c;d"), Arrays.asList("a;b","c;d") },
                {  Arrays.asList("--jvmArgs", "{a,b,c,d}"), Arrays.asList("a,b,c,d") },
                {  Arrays.asList("--jvmArgs", "{a;b;c;d}"), Arrays.asList("{a;b;c;d}") }, // no separators so region markers will not be removed
                {  Arrays.asList("--jvmArgs", "{a,b},{c,d},{e,f}"), Arrays.asList("a,b","c,d","e,f") },
                {  Arrays.asList("--jvmArgs", "{a,b@c,d}"), Arrays.asList("a,b@c,d") }, // pre-existing '@' will not be replaced with ","
        });
    }

    private final List<String> input;
    private final List<String> expected;


    public CommaAwareArgsProcessorTest(List<String> input, List<String> expected) {
        this.input = input;
        this.expected = expected;
    }

    @Test
    public void parseArgs() {
        OptionParser optionParser = new OptionParser();
        ArgumentAcceptingOptionSpec<String> args = optionParser
                .accepts(ConfigOption.CHILD_JVM.getParamName())
                .withRequiredArg()
                .describedAs("jvm args for child JVM");
        ArgumentAcceptingOptionSpec<String> unrelatedArgs = optionParser
                .accepts("unrelated")
                .withRequiredArg()
                .describedAs("unrelatedArgs");

        final OptionSet userArgs = optionParser.parse(this.input.toArray(new String[0]));
        Assert.assertEquals(this.expected, new CommaAwareArgsProcessor(args).values(userArgs));
    }

}
