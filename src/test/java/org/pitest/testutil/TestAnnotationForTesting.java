package org.pitest.testutil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface TestAnnotationForTesting {

  public abstract class NONE extends Throwable {
    private static final long serialVersionUID = 1L;
  }

  Class<? extends Throwable> expected() default NONE.class;

}
