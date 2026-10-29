package org.pitest.mutationtest.commandline;

import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Splits the values of the features option on the commas that separate features,
 * so that a feature parameter may contain a comma.
 *
 * A comma inside a parameter list or a parameter value, as in
 * {@code +FOO(file[/tmp/a,b/c.txt]),-BAR()}, is part of the value and does not
 * split the option. Commas inside a quoted value are kept as well.
 */
public class FeatureArgsProcessor {

    private final OptionSpec<String> option;

    public FeatureArgsProcessor(OptionSpec<String> option) {
        this.option = option;
    }

    public List<String> values(OptionSet userArgs) {
        final List<String> raw = option.values(userArgs);
        if (raw.isEmpty()) {
            return Collections.emptyList();
        }
        final List<String> result = new ArrayList<>();
        for (final String value : raw) {
            split(value, result);
        }
        return result;
    }

    private static void split(String value, List<String> result) {
        int depth = 0;
        boolean quoted = false;
        int start = 0;
        for (int i = 0; i < value.length(); i++) {
            final char c = value.charAt(i);
            if (c == '\\' && quoted && i + 1 < value.length()) {
                i++;
            } else if (c == '"') {
                quoted = !quoted;
            } else if (!quoted) {
                if (c == '(' || c == '[') {
                    depth++;
                } else if (c == ')' || c == ']') {
                    depth--;
                } else if (c == ',' && depth == 0) {
                    result.add(value.substring(start, i).trim());
                    start = i + 1;
                }
            }
        }
        result.add(value.substring(start).trim());
    }
}
