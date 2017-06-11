package org.pitest.classinfo;

import java.util.LinkedHashMap;
import java.util.Map;

import org.pitest.functional.Option;

public class CachingByteArraySource implements ClassByteArraySource {
  
  private final ClassByteArraySource child;
  private final Map<String,Option<byte[]>> cache;

  public CachingByteArraySource(ClassByteArraySource child, int maxSize) {
    this.child = child;
    this.cache = new FixedSizeHashMap<String,Option<byte[]>>(maxSize);
  }

  @Override
  public Option<byte[]> getBytes(String clazz) {
    Option<byte[]> maybeBytes = cache.get(clazz);
    if (maybeBytes != null) {
      return maybeBytes;
    }
    
    maybeBytes  = child.getBytes(clazz);
    cache.put(clazz, maybeBytes);
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
    return size() > maxsize;
}
};
