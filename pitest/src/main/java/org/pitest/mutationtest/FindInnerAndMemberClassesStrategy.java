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
package org.pitest.mutationtest;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.pitest.functional.F;
import org.pitest.internal.ClassPathByteArraySource;
import org.pitest.reflection.Reflection;

public class FindInnerAndMemberClassesStrategy implements
    F<Class<?>, Collection<String>> {

  public Collection<String> apply(final Class<?> a) {
    final Set<String> ss = new HashSet<String>();
    ss.add(a.getName());
    ss.addAll(Reflection.allInnerClasses(a, new ClassPathByteArraySource()));
    return ss;
  }

}
