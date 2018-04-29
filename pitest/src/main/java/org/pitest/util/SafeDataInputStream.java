/*
 * Copyright 2011 Henry Coles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class SafeDataInputStream {

  private final DataInputStream dis;

  public SafeDataInputStream(final InputStream is) {
    this.dis = new DataInputStream(is);
  }

  public int readInt() {
    try {
      return this.dis.readInt();
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

  public String readString() {
    try {
      return new String(readBytes(), "UTF-8");
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

  public byte[] readBytes() {
    try {
      final int length = this.dis.readInt();
      final byte[] data = new byte[length];
      this.dis.readFully(data);
      return data;
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends Serializable> T read(final Class<T> type) {
    try {
      return (T) deserialize(readBytes());
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

  public void close() {
    try {
      this.dis.close();
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

  public byte readByte() {
    try {
      return this.dis.readByte();
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

  public boolean readBoolean() {
    try {
      return this.dis.readBoolean();
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

  public long readLong() {
    try {
      return this.dis.readLong();
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

  private Object deserialize(byte[] bytes) throws IOException {
    final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
    ObjectInput in = null;
    try {
      in = new ObjectInputStream(bis);
      return in.readObject();
    } catch (final ClassNotFoundException e) {
      throw Unchecked.translateCheckedException(e);
    } finally {
        if (in != null) {
          in.close();
        }
    }
  }

}
