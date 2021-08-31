package org.pitest.mutationtest.engine.gregor.config;

import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface MutatorGroup {
    void register(Map<String, List<MethodMutatorFactory>> mutators);

    default List<MethodMutatorFactory> gather(Map<String, List<MethodMutatorFactory>> mutators, String...keys) {
        return Arrays.stream(keys)
                .flatMap(k -> mutators.get(k).stream())
                .collect(Collectors.toList());
    }
}