package org.pitest.aggregate;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;

final class MutatorUtil {

  private static final Map<String, MethodMutatorFactory> FACTORIES = new ConcurrentHashMap<>();

  @SuppressWarnings("unchecked")
  static MethodMutatorFactory loadMutator(final String className) {
    if (!FACTORIES.containsKey(className)) {
      try {
        final Class<MethodMutatorFactory> clazz = (Class<MethodMutatorFactory>) Class.forName(className);
        final Method values = clazz.getMethod("values");
        final Object valuesArray = values.invoke(null);
        final MethodMutatorFactory mutator = (MethodMutatorFactory) Array.get(valuesArray, 0);
        FACTORIES.put(className, mutator);
      } catch (final Exception e) {
        throw new RuntimeException("Unable to load Mutator for class: " + className, e);
      }
    }
    return FACTORIES.get(className);
  }
}
