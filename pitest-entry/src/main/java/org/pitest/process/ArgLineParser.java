package org.pitest.process;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.StringTokenizer;

import static org.pitest.process.ArgLineParser.State.START;

/**
 * Simple state machine to split arglines into sections. Arglines may
 * contain single or double quotes, which might be escaped.
 */
public class ArgLineParser {

    private static final String ESCAPE_CHAR = "\\";
    private static final String SINGLE_QUOTE = "\'";
    public static final String DOUBLE_QUOTE = "\"";

    public static List<String> split(String in) {
        return process(stripWhiteSpace(in));
    }

    private static List<String> process(String in) {
        if (in.isEmpty()) {
            return Collections.emptyList();
        }

        final StringTokenizer tokenizer = new StringTokenizer(in, "\"\' \\", true);
        List<String> tokens = new ArrayList<>();

        Deque<State> state = new ArrayDeque<>();
        state.push(START);
        StringBuilder current = new StringBuilder();
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            switch (state.peek()) {
                case START:
                    if (token.equals(SINGLE_QUOTE)) {
                        state.push(State.IN_QUOTE);
                    } else if (token.equals(DOUBLE_QUOTE)) {
                        state.push(State.IN_DOUBLE_QUOTE);
                    } else if (token.equals(" ")) {
                        if (current.length() != 0) {
                            tokens.add(current.toString());
                            current = new StringBuilder();
                        }
                    } else {
                        current.append(token);
                        if (token.equals(ESCAPE_CHAR)) {
                            state.push(State.IN_ESCAPE);
                        }
                    }
                    break;
                case IN_QUOTE:
                    if (token.equals(SINGLE_QUOTE)) {
                        state.pop();
                    } else {
                        current.append(token);
                        if (token.equals(ESCAPE_CHAR)) {
                            state.push(State.IN_ESCAPE);
                        }
                    }
                    break;
                case IN_DOUBLE_QUOTE:
                    if (token.equals(DOUBLE_QUOTE)) {
                        state.pop();
                    } else {
                        current.append(token);
                        if (token.equals(ESCAPE_CHAR)) {
                            state.push(State.IN_ESCAPE);
                        }
                    }
                    break;
                case IN_ESCAPE:
                    current.append(token);
                    if (!token.equals(ESCAPE_CHAR)) {
                        state.pop();
                    }
                    break;
            }
        }

        if (current.length() != 0) {
            tokens.add(current.toString());
        }

        if (state.size() != 1) {
            throw new RuntimeException("Unclosed quote in " + in);
        }

        return tokens;
    }

    private static String stripWhiteSpace(String in) {
        if (in == null) {
            return "";
        }
        return in.replaceAll("\\s", " ").trim();
    }

    enum State {
        START, IN_ESCAPE, IN_QUOTE, IN_DOUBLE_QUOTE
    }
}
