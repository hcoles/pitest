package org.pitest.mutationtest.engine.gregor.mutators.returns;

import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.config.MutatorGroup;

import java.util.List;
import java.util.Map;

public class ReturnsMutatorGroup implements MutatorGroup {
    @Override
    public void register(Map<String, List<MethodMutatorFactory>> mutators) {
        mutators.put("RETURNS",
                gather(mutators,"TRUE_RETURNS",
                        "FALSE_RETURNS",
                        "PRIMITIVE_RETURNS",
                        "EMPTY_RETURNS",
                        "NULL_RETURNS"));

    }
}
