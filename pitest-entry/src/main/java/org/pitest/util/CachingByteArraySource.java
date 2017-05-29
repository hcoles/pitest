package org.pitest.util;

import java.util.LinkedHashMap;
import java.util.Map;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.functional.Option;

public class CachingByteArraySource implements ClassByteArraySource {
  
  private final ClassByteArraySource child;
  private final Map<String,Option<byte[]>> cache = new LinkedHashMap<String,Option<byte[]>>() {
    private static final long serialVersionUID = 2648931151905594122L;
    @Override
    protected boolean removeEldestEntry(Map.Entry <String,Option<byte[]>>eldest) {
      return size() > 100;
  }
  };

  public CachingByteArraySource(ClassByteArraySource child) {
    this.child = child;
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
