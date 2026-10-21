package org.pitest.mutationtest.engine.gregor.mutators.jspecify;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Test stand-in for {@code org.jspecify.annotations.Nullable}. Declared
 * type-use only, like the real annotation, so a return type annotation lands in
 * RuntimeVisibleTypeAnnotations.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE_USE)
public @interface Nullable {
}
