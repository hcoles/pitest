package org.pitest.mutationtest.engine.gregor.mutators.jspecify;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Test stand-in for {@code org.jspecify.annotations.NullUnmarked}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD})
public @interface NullUnmarked {
}
