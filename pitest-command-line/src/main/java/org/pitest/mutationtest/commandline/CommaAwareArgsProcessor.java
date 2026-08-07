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

        Set<Integer> commaIndices = new HashSet<>();
        Set<Integer> markerIndices = new HashSet<>();
        String preprocessedOptions = replaceCommas(commandLineOption, commaIndices, markerIndices);

        String[] arguments = preprocessedOptions.split(",");
        return postProcess(commaIndices, markerIndices, arguments);
    }

    /**
     * Put commas back and delete region marker.
     */
    private List<String> postProcess(
            Set<Integer> commaIndices,
            Set<Integer> markerIndices,
            String[] arguments) {
        List<String> newArguments = new ArrayList<>();
        int currentArgumentLocation = 0;
        for (String argument : arguments) {
            newArguments.add(buildNewArgument(commaIndices, markerIndices, currentArgumentLocation, argument).toString());
            currentArgumentLocation += argument.length() + 1;
        }
        return newArguments;
    }

    private StringBuilder buildNewArgument(
            Set<Integer> commaIndices,
            Set<Integer> markerIndices,
            int base,
            String argument) {
        StringBuilder newArgument = new StringBuilder();
        for (int j = 0; j < argument.length(); j++) {
            char current = argument.charAt(j);
            int originalIndex = j + base;

            if (markerIndices.contains(originalIndex)) {
                continue;
            }

            if (current == '@' && commaIndices.contains(originalIndex)) {
                newArgument.append(',');
            } else {
                newArgument.append(current);
            }
        }
        return newArgument;
    }

    private String replaceCommas(
            String single,
            Set<Integer> commaIndices,
            Set<Integer> markerIndices) {
        StringBuilder newString = new StringBuilder();
        boolean inSpecialRegion = false;
        boolean regionEscapedComma = false;
        int regionStart = -1;

        for (int i = 0; i < single.length(); i++) {
            char current = single.charAt(i);
            char tobeAdded = current;

            if (current == REGION_BEGIN && !inSpecialRegion) {
                inSpecialRegion = true;
                regionEscapedComma = false;
                regionStart = i;
            } else if (current == REGION_END && inSpecialRegion) {
                if (regionEscapedComma) {
                    markerIndices.add(regionStart);
                    markerIndices.add(i);
                }
                inSpecialRegion = false;
                regionEscapedComma = false;
                regionStart = -1;
            } else if (inSpecialRegion && current == ',') {
                tobeAdded = '@';
                commaIndices.add(i);
                regionEscapedComma = true;
            }

            newString.append(tobeAdded);
        }

        return newString.toString();
    }

}
