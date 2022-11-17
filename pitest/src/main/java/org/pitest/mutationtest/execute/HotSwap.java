package org.pitest.mutationtest.execute;

import org.pitest.boot.HotSwapAgent;
import org.pitest.classinfo.ClassName;
import org.pitest.util.Unchecked;

/**
 * Since pitest 1.9.4 there is an implicit assumption that pitest will never mutate
 * more than once class within the same jvm. If this assumption is ever broken, mutants
 * from the previous class would remain active and invalidate results.
 */
class HotSwap {

  public Boolean insertClass(final ClassName clazzName, ClassLoader loader, final byte[] mutantBytes) {
    try {
      // Some frameworks (eg quarkus) run tests in non delegating
      // classloaders. Need to make sure these are transformed too
      CatchNewClassLoadersTransformer.setMutant(clazzName.asInternalName(), mutantBytes);

      // trigger loading for the current loader
      Class<?> clazz = Class.forName(clazzName.asJavaName(), false, loader);

      // will still need to explicitly swap it... not clear why the transformed does not do this
      return HotSwapAgent.hotSwap(clazz, mutantBytes);

    } catch (final ClassNotFoundException e) {
      throw Unchecked.translateCheckedException(e);
    }

  }

}
