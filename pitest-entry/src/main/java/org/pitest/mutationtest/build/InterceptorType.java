package org.pitest.mutationtest.build;

/**
 * Used to group interceptors. Running order of groups
 * is guaranteed (by order within group is not).
 *
 * The name indicates the intended typees of behaviour - but this is not enforced.
 *
 * OTHER -
 * MODIFY - Modify mutants in a way that is functionally significant (e.g mark as poisoning JVM)
 * PRE_SCAN_FILTER - Remove mutants from processing, in prescan and main scan
 * FILTER - Remove mutants from processing
 * MODIFY_COSMETIC - Modify mutants in way that will not affect processing (e.g update descriptions)
 * REPORT - Output mutant in their final state
 *
 */
public enum InterceptorType {
  OTHER(true),
  MODIFY(true),
  PRE_SCAN_FILTER(true),
  FILTER(false),
  MODIFY_COSMETIC(false),
  REPORT(false);

  private final boolean includeInPrescan;

  InterceptorType(boolean includeInPrescan) {
    this.includeInPrescan = includeInPrescan;
  }

  public boolean includeInPrescan() {
    return includeInPrescan;
  }
}
