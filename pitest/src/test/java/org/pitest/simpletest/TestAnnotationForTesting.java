package org.pitest.simpletest;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface TestAnnotationForTesting {

  public class NONE extends Throwable {
    private NONE() {
    }

    private static final long serialVersionUID = 1L;
  }

  Class<? extends Throwable> expected() default NONE.class;

}
