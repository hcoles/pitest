package org.pitest.mutationtest.execute;

import org.pitest.boot.HotSwapAgent;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassName;
import org.pitest.functional.F3;
import org.pitest.util.Unchecked;

class HotSwap implements F3<ClassName, ClassLoader, byte[], Boolean> {

  private final ClassByteArraySource byteSource;
  private byte[]                     lastClassPreMutation;
  private ClassName                  lastMutatedClass;

  HotSwap(final ClassByteArraySource byteSource) {
    this.byteSource = byteSource;
  }

  public Boolean apply(final ClassName clazzName, final ClassLoader loader,
      final byte[] b) {
    Class<?> clazz;
    try {
      restoreLastClass(this.byteSource, clazzName, loader);
      clazz = Class.forName(clazzName.asJavaName(), false, loader);
      return HotSwapAgent.hotSwap(clazz, b);
    } catch (final ClassNotFoundException e) {
      throw Unchecked.translateCheckedException(e);
    }

  }

  private void restoreLastClass(final ClassByteArraySource byteSource,
      final ClassName clazzName, final ClassLoader loader)
      throws ClassNotFoundException {
    Class<?> clazz;
    if ((this.lastMutatedClass != null)
        && !this.lastMutatedClass.equals(clazzName)) {
      clazz = Class.forName(this.lastMutatedClass.asJavaName(), false, loader);
      HotSwapAgent.hotSwap(clazz, this.lastClassPreMutation);
    }

    if ((this.lastMutatedClass == null)
        || !this.lastMutatedClass.equals(clazzName)) {
      this.lastClassPreMutation = byteSource.apply(clazzName.asJavaName())
          .value();
    }

    this.lastMutatedClass = clazzName;
  }

}
