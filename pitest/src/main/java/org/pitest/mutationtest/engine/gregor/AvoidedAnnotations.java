package org.pitest.mutationtest.engine.gregor;

/**
 * Annotation to avoid mutation for. Hard coded for the moment.
 */
class AvoidedAnnotations {
  static boolean shouldAvoid(String desc) {
    return desc.endsWith("Generated;") 
        || desc.endsWith("DoNotMutate;") 
        || desc.endsWith("CoverageIgnore;"); 
  }

}
