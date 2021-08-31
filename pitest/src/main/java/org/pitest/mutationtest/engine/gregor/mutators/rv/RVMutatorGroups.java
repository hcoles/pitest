package org.pitest.mutationtest.engine.gregor.mutators.rv;

import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.config.MutatorGroup;

import java.util.List;
import java.util.Map;

public class RVMutatorGroups implements MutatorGroup {
    @Override
    public void register(Map<String, List<MethodMutatorFactory>> mutators) {
        /*
         * mutators that mutate binary arithmetic operations.
         */
        mutators.put("AOR", gather(mutators,"AOR1", "AOR2", "AOR3", "AOR4"));

        /*
         * mutators that replace a binary arithmetic operations with one of its members.
         */
        mutators.put("AOD", gather(mutators,"AOD1", "AOD2"));

        /*
         * mutators that replace an inline constant a with 0, 1, -1, a+1 or a-1 .
         */
        mutators.put("CRCR", gather(mutators,"CRCR1", "CRCR2", "CRCR3", "CRCR4",
                "CRCR5", "CRCR6"));
        /*
         * mutators that replace an bitwise ands and ors.
         */
        mutators.put("OBBN", gather(mutators,"OBBN1", "OBBN2", "OBBN3"));

        /*
         * mutators that replace conditional operators.
         */
        mutators.put("ROR", gather(mutators,"ROR1", "ROR2", "ROR3", "ROR4", "ROR5"));

        /*
         * mutators that insert increments.
         */
        mutators.put("UOI", gather(mutators,"UOI1", "UOI2", "UOI3", "UOI4"));
    }
}
