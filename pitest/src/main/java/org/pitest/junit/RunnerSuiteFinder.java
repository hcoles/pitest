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

package org.pitest.junit;

import java.util.Collection;

import org.junit.internal.runners.SuiteMethod;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.pitest.extension.TestSuiteFinder;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.Option;
import org.pitest.functional.Prelude;
import org.pitest.functional.predicate.Predicate;
import org.pitest.internal.TestClass;
import org.pitest.junit.adapter.AdaptedJUnitTestUnit;

public class RunnerSuiteFinder implements TestSuiteFinder {

  @SuppressWarnings("unchecked")
  public Collection<TestClass> apply(final TestClass a) {

    final Runner runner = AdaptedJUnitTestUnit.createRunner(a.getClazz());
    
    FunctionalList<TestClass> classes =  FCollection.filter(runner.getDescription().getChildren(), Prelude.or(isSuiteMethodRunner(runner),isSuite()))
        .flatMap(descriptionToTestClass());
    classes.remove(a);
    return classes;

  }

  private static Predicate<Description> isSuiteMethodRunner(final Runner runner) {
    return new Predicate<Description>() {
      public Boolean apply(Description a) {
        return SuiteMethod.class.isAssignableFrom(runner.getClass());
      }
      
    };
  }

  private static F<Description, Option<TestClass>> descriptionToTestClass() {
    return new F<Description, Option<TestClass>>() {

      public Option<TestClass> apply(final Description a) {
        final Class<?> clazz = a.getTestClass();
        if (clazz != null) {
          return Option.some(new TestClass(clazz));
        } else {
          return Option.<TestClass> none();
        }
      }

    };
  }

  private static Predicate<Description> isSuite() {
    return new  Predicate<Description> () {
      public Boolean apply(final Description a) {
        return a.isSuite();
      }

    };
  }

}
