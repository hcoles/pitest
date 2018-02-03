package org.pitest.mutationtest.build;

/**
 * Used to group interceptors. Running order of groups
 * is guaranteed (by order within group is not).
 *
 * The name indicates the intended typees of behaviour - but this is not enforced.
 *
 * OTHER -
 * MODIFY - Modify mutants in a way that is functionally significant (e.g mark as poisoning JVM)
 * FILTER - Remove mutants from processing
 * MODIFY_COSMETIC - Modify mutants in way that will not affect processing (e.g update descriptions)
 * REPORT - Output mutant in their final state
 *
 */
public enum InterceptorType {
  OTHER, MODIFY, FILTER, MODIFY_COSMETIC, REPORT
}
