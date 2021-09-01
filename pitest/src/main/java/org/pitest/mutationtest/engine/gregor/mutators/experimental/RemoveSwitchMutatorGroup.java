package org.pitest.mutationtest.engine.gregor.mutators.experimental;

import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.config.MutatorGroup;

import java.util.List;
import java.util.Map;

public class RemoveSwitchMutatorGroup implements MutatorGroup {
    @Override
    public void register(Map<String, List<MethodMutatorFactory>> mutators) {
        mutators.put("REMOVE_SWITCH", RemoveSwitchMutator.makeMutators());
    }
}
