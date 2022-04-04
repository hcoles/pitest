package org.pitest.mutationtest.commandline;

import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * JVM args preprocessor to allow single arguments with commas.
 * The processor will first replace all commas with '@' if the comma is within a region of the argument marked by { and }.
 * The processor will then split up the arguments by commas that are outside the markers.
 * As a final step the '@' are replaced with commas again.
 */
public class CommaAwareArgsProcessor {

    private static final char REGION_BEGIN = '{';
    private static final char REGION_END = '}';
    private final OptionSpec<String> optionsSpec;

    public CommaAwareArgsProcessor(OptionSpec<String> args) {
        this.optionsSpec = args;
    }

    public List<String> values(OptionSet userArgs) {
        final String commandLineOption = optionsSpec.value(userArgs);
        if (commandLineOption == null) {
            return Collections.emptyList();
        }

        Set<Integer> modifiedIndices = new HashSet<>();
        String preprocessedOptions = replaceCommas(commandLineOption, modifiedIndices);

        String[] arguments = preprocessedOptions.split(",");
        return postProcess(modifiedIndices, arguments);
    }

    /**
     * Put commas back and delete region marker.
     */
    private List<String> postProcess(Set<Integer> modifiedIndices, String[] arguments) {
        List<String> newArguments = new ArrayList<>();
        int base = 0;
        for (String argument : arguments) {
            newArguments.add(buildNewArgument(modifiedIndices, base, argument).toString());
            base += argument.length() + 1;
        }
        return newArguments;
    }

    private StringBuilder buildNewArgument(Set<Integer> modifiedIndices, int base, String argument) {
        StringBuilder newArgument = new StringBuilder();
        for (int j = 0; j < argument.length(); j++) {
            char current = argument.charAt(j);

            // Only remove region markers, if commas have been replaced. Otherwise treat them as part of the argument.
            if (!modifiedIndices.isEmpty() && (current == REGION_BEGIN || current == REGION_END)) {
                continue;
            }
            if (current == '@' && modifiedIndices.contains(j + base)) {
                newArgument.append(',');
            } else {
                newArgument.append(current);
            }
        }
        return newArgument;
    }

    private String replaceCommas(String single, Set<Integer> modifiedIndices) {
        StringBuilder newString = new StringBuilder();
        boolean inSpecialRegion = false;
        for (int i = 0; i < single.length(); i++) {
            char current = single.charAt(i);
            char tobeAdded = current;
            if (current == REGION_BEGIN && !inSpecialRegion) {
                inSpecialRegion = true;
            } else if (current == REGION_END && inSpecialRegion) {
                inSpecialRegion = false;
            } else if (inSpecialRegion && current == ',') {
                tobeAdded = '@';
                modifiedIndices.add(i);
            }
            newString.append(tobeAdded);
        }
        return newString.toString();
    }


}
