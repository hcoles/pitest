/*
 * Copyright 2010 Henry Coles
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
package org.pitest.internal.isolation;

public class IsolatedLong {

  public static Long getLong(final String nm) { // NO_UCD
    return getLong(nm, null);
  }

  public static Long getLong(final String nm, final long val) { // NO_UCD
    final Long result = Long.getLong(nm, null);
    return (result == null) ? Long.valueOf(val) : result;
  }

  public static Long getLong(final String nm, final Long val) { // NO_UCD
    String v = null;
    try {
      v = IsolatedSystem.getProperty(nm);
    } catch (final IllegalArgumentException e) {
    } catch (final NullPointerException e) {
    }
    if (v != null) {
      try {
        return Long.decode(v);
      } catch (final NumberFormatException e) {
      }
    }
    return val;
  }

}
