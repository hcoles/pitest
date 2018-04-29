package org.pitest.classinfo;

import java.util.LinkedHashMap;
import java.util.Map;

import java.util.Optional;

public class CachingByteArraySource implements ClassByteArraySource {

  private final ClassByteArraySource child;
  private final Map<String,Optional<byte[]>> cache;

  public CachingByteArraySource(ClassByteArraySource child, int maxSize) {
    this.child = child;
    this.cache = new FixedSizeHashMap<>(maxSize);
  }

  @Override
  public Optional<byte[]> getBytes(String clazz) {
    Optional<byte[]> maybeBytes = this.cache.get(clazz);
    if (maybeBytes != null) {
      return maybeBytes;
    }

    maybeBytes  = this.child.getBytes(clazz);
    this.cache.put(clazz, maybeBytes);
    return maybeBytes;

  }

}

class FixedSizeHashMap<K,V> extends LinkedHashMap<K,V> {
  private final int maxsize;
  FixedSizeHashMap(int maxsize) {
    this.maxsize = maxsize;
  }
  private static final long serialVersionUID = 2648931151905594122L;
  @Override
  protected boolean removeEldestEntry(Map.Entry <K,V> eldest) {
    return size() > this.maxsize;
}
};
