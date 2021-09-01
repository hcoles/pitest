package org.pitest.mutationtest.engine.gregor.mutators;

import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.config.MutatorGroup;

import java.util.List;
import java.util.Map;

public class RemoveConditionalMutatorGroup implements MutatorGroup {

    @Override
    public void register(Map<String, List<MethodMutatorFactory>> mutators) {
        mutators.put("REMOVE_CONDITIONALS",
                gather(mutators,"REMOVE_CONDITIONALS_EQUAL_IF",
                        "REMOVE_CONDITIONALS_EQUAL_IF",
                        "REMOVE_CONDITIONALS_EQUAL_ELSE",
                        "REMOVE_CONDITIONALS_ORDER_IF",
                        "REMOVE_CONDITIONALS_ORDER_ELSE"));

    }

}
