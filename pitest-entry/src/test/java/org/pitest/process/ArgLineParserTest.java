package org.pitest.process;

import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

public class ArgLineParserTest {

    @Test
    public void parsesNullToEmptyList() {
        List<String> actual = ArgLineParser.split(null);
        assertThat(actual).isEmpty();
    }

    @Test
    public void parsesEmptyStringToEmptyList() {
        List<String> actual = ArgLineParser.split("");
        assertThat(actual).isEmpty();
    }

    @Test
    public void parsesWhiteSpaceToEmptyList() {
        List<String> actual = ArgLineParser.split(" ");
        assertThat(actual).isEmpty();
    }

    @Test
    public void parsesUnquotedItems() {
        List<String> actual = ArgLineParser.split("foo bar");
        assertThat(actual).containsExactly("foo", "bar");
    }

    @Test
    public void parsesUnquotedItemsWithIrregularWhiteSpace() {
        List<String> actual = ArgLineParser.split("foo    bar       car");
        assertThat(actual).containsExactly("foo", "bar", "car");
    }

    @Test
    public void parsesDoubleQuotedItems() {
        List<String> actual = ArgLineParser.split("foo \" in double quotes \" bar");
        assertThat(actual).containsExactly("foo", " in double quotes ", "bar");
    }

    @Test
    public void parsesSingleQuotedItems() {
        List<String> actual = ArgLineParser.split("foo ' in single quotes   ' bar");
        assertThat(actual).containsExactly("foo", " in single quotes   ", "bar");
    }

    @Test
    public void parsesDoubleQuoteWithinSingleQuote() {
        List<String> actual = ArgLineParser.split("foo ' \" ' bar");
        assertThat(actual).containsExactly("foo", " \" ", "bar");
    }

    @Test
    public void parsesSingleQuoteWithinDoubleQuote() {
        List<String> actual = ArgLineParser.split("foo \" ' \" bar");
        assertThat(actual).containsExactly("foo", " ' ", "bar");
    }

    @Test
    public void escapedDoubleQuoteRemainsEscaped() {
        List<String> actual = ArgLineParser.split("foo \\\" bar");
        assertThat(actual).containsExactly("foo", "\\\"" , "bar");
    }

    @Test
    public void escapedSingleQuoteRemainsEscaped() {
        List<String> actual = ArgLineParser.split("foo \\' bar");
        assertThat(actual).containsExactly("foo", "\\'" , "bar");
    }

    @Test
    public void escapesEscapeChar() {
        List<String> actual = ArgLineParser.split("foo \\\\' bar");
        assertThat(actual).containsExactly("foo", "\\\\'" , "bar");
    }

    @Test
    public void escapedDoubleQuoteInDoubleQuotesRemainsEscaped() {
        List<String> actual = ArgLineParser.split("foo \"bar\\\" car\" la");
        assertThat(actual).containsExactly("foo", "bar\\\" car", "la");
    }

    @Test
    public void escapedSingleQuoteInDoubleQuotesRemainsEscaped() {
        List<String> actual = ArgLineParser.split("foo \"bar\\' car\" la");
        assertThat(actual).containsExactly("foo", "bar\\' car", "la");
    }

    @Test
    public void escapedSingleQuoteInSingleQuotesRemainsEscaped() {
        List<String> actual = ArgLineParser.split("foo 'bar\\' car' la");
        assertThat(actual).containsExactly("foo", "bar\\' car", "la");
    }

    @Test
    public void escapedDoubleQuoteInSingleQuotesRemainsEscaped() {
        List<String> actual = ArgLineParser.split("foo 'bar\\\" car' la");
        assertThat(actual).containsExactly("foo", "bar\\\" car", "la");
    }

    @Test
    public void multipleEscapedQuotesRemainEscaped() {
        List<String> actual = ArgLineParser.split("foo 'bar\\\" \\\" \\' car' la");
        assertThat(actual).containsExactly("foo", "bar\\\" \\\" \\' car", "la");
    }

    @Test
    public void errorsOnUnclosedSingleQuote() {
        assertThatCode(() -> ArgLineParser.split("foo '"))
                .hasMessageContaining("Unclosed quote");
    }

    @Test
    public void errorsOnUnclosedDoubleQuote() {
        assertThatCode(() -> ArgLineParser.split("foo \""))
                .hasMessageContaining("Unclosed quote");
    }

    @Test
    public void handlesRealExampleArgLine() {
        String argLine = "-Dfile.encoding=UTF-8\n" +
                "      -Dnet.bytebuddy.experimental=true\n" +
                "      --add-opens=java.base/java.lang=ALL-UNNAMED\n" +
                "      --add-opens=java.base/java.math=ALL-UNNAMED\n";

        List<String> actual = ArgLineParser.split(argLine);
        assertThat(actual).containsExactly("-Dfile.encoding=UTF-8",
                "-Dnet.bytebuddy.experimental=true",
                "--add-opens=java.base/java.lang=ALL-UNNAMED",
                "--add-opens=java.base/java.math=ALL-UNNAMED");
    }

}