/*
 * Copyright 2012 Peter Lawrey
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
package org.pitest.boot;

import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * Thread safe BitSet implementation
 */
public class AtomicBitSet {

  private final AtomicIntegerArray array;
  private final int                length;

  public AtomicBitSet(final int length) {
    this.length = length;
    final int intLength = (length + 31) / 32;
    this.array = new AtomicIntegerArray(intLength);
  }

  public void set(final long n) {
    final int bit = 1 << n;
    final int idx = (int) (n >>> 5);
    while (true) {
      final int num = this.array.get(idx);
      final int num2 = num | bit;
      if ((num == num2) || this.array.compareAndSet(idx, num, num2)) {
        return;
      }
    }
  }

  public boolean get(final long n) {
    final int bit = 1 << n;
    final int idx = (int) (n >>> 5);
    final int num = this.array.get(idx);
    return (num & bit) != 0;
  }

  public int length() {
    return this.length;
  }
}
