package org.pitest.classinfo;

import java.util.zip.Adler32;

public class AddlerHash implements HashFunction {

  public long hash(byte[] value) {
    Adler32 adler = new Adler32();
    adler.update(value);
    return adler.getValue();
  }
}
