package org.pitest.mutationtest.build.intercept.jspecify;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Test stand-in for {@code org.jspecify.annotations.NullMarked}. The mutator
 * matches annotations by simple name, so the tests do not need the JSpecify
 * dependency.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD})
public @interface NullMarked {
}
