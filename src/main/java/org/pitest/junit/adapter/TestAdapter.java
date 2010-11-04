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
package org.pitest.junit.adapter;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Callable;

import junit.framework.TestCase;

import org.pitest.Description;
import org.pitest.TestMethod;
import org.pitest.extension.ResultCollector;
import org.pitest.extension.TestFilter;
import org.pitest.extension.TestUnit;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.internal.IsolationUtils;
import org.pitest.reflection.IsNamed;
import org.pitest.reflection.Reflection;
import org.pitest.testunit.AbstractTestUnit;

public class TestAdapter extends AbstractTestUnit {
  
  private final TestCase test;
  
  public TestAdapter(TestCase testCase ) {
    super(testCaseToDescription(testCase));
    this.test = testCase;
  }

  private static Description testCaseToDescription(TestCase testCase) {
    Collection<Method> ms = Reflection.allMethods(testCase.getClass());
    Method m = FCollection.filter(ms, IsNamed.instance(testCase.getName())).get(0);
    return new Description(testCase.getName(),testCase.getClass(), new TestMethod(m,null));
  }

 

  public void execute(ClassLoader loader, ResultCollector rc) {
    try {
      rc.notifyStart(description());
      if (IsolationUtils.fromDifferentLoader(test.getClass(), loader)) {
        executeInDifferentClassLoader(loader, rc);
      }  else {
        test.runBare();
        rc.notifyEnd(description());
      }
    } catch (Throwable t) {
      rc.notifyEnd(description(),t);
    }   
  }

  @SuppressWarnings("unchecked")
  private void executeInDifferentClassLoader(ClassLoader loader, ResultCollector rc) {
    Callable<Throwable> c = new Callable<Throwable>() {
      public Throwable call() throws Exception {
        try {
        test.runBare();
        } catch (Throwable t) {
          return t;
        }
        return null;
      }
      
    };
    
    Callable<Throwable>  foreignC = (Callable<Throwable>) IsolationUtils.cloneForLoader(c, loader);
    try {
      Throwable result = foreignC.call();
      rc.notifyEnd(description(), result);
    } catch (Exception e) {
      rc.notifyEnd(description(),e);
    }
    
  }

  
  

}
