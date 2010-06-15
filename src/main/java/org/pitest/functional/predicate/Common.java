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
package org.pitest.functional.predicate;

/**
 * @author henry
 * 
 */
public abstract class Common {

  public static <T> Predicate<T> fIsNotNull() {
    return new Predicate<T>() {
      public Boolean apply(final T o) {
        return (o != null);
      }
    };
  }

  public static <T> Predicate<T> fIsNull() {
    return new Predicate<T>() {
      public Boolean apply(final T o) {
        return (o == null);
      }
    };
  }

}
