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

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import org.junit.Test;

public class SafeDataInputStreamTest {

  @Test
  public void shouldBeAbletoReadLargeStrings() {
    final char[] chars = new char[65536];
    Arrays.fill(chars, '!');
    final String s = new String(chars);

    final ByteArrayOutputStream o = new ByteArrayOutputStream();
    final SafeDataOutputStream dos = new SafeDataOutputStream(o);
    dos.writeString(s);

    final ByteArrayInputStream i = new ByteArrayInputStream(o.toByteArray());

    final SafeDataInputStream dis = new SafeDataInputStream(i);
    final String actual = dis.readString();

    assertEquals(s, actual);

  }

}
