///*
// * Based on http://code.google.com/p/javacoveragent/ by
// * "alex.mq0" and "dmitry.kandalov"
// * 
// * 
// * Licensed under the Apache License, Version 2.0 (the "License"); 
// * you may not use this file except in compliance with the License. 
// * You may obtain a copy of the License at 
// * 
// * http://www.apache.org/licenses/LICENSE-2.0 
// * 
// * Unless required by applicable law or agreed to in writing, 
// * software distributed under the License is distributed on an "AS IS" BASIS, 
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// * See the License for the specific language governing permissions and limitations under the License. 
// */
//
//package org.pitest.coverage.codeassist;
//
//import static junit.framework.Assert.assertEquals;
//import static org.pitest.coverage.codeassist.ClassUtils.classAsBytes;
//
//import java.lang.reflect.Method;
//import java.security.ProtectionDomain;
//import java.util.Collections;
//
//import org.junit.Test;
//import org.pitest.coverage.calculator.CodeCoverageStore;
//import org.pitest.coverage.calculator.InvokeEntry;
//import org.pitest.coverage.calculator.InvokeQueue;
//import org.pitest.coverage.calculator.InvokeStatistics;
//import org.pitest.coverage.calculator.InvokeType;
//import org.pitest.coverage.codeassist.ClassTransformer;
//
//
///**
// * User: dima Date: Feb 8, 2009 Time: 2:33:05 PM
// */
//public class ClassTransformerTest {
//  private static final String           SAMPLE_CLASS_NAME = SampleClass.class
//                                                              .getName();
//  private static final ClassLoader      DUMMY_LOADER      = null;
//  private static final Class<?>            DUMMY_CLASS       = null;
//  private static final ProtectionDomain DUMMY_DOMAIN      = null;
//
//  @Test
//  public void shouldInstrumentClasses() throws Exception {
//    // setup
//    final InvokeStatistics invokeStatistics = new InvokeStatistics();
//    final InvokeQueue invokeQueue = new InvokeQueue();
//    CodeCoverageStore.init(invokeQueue, invokeStatistics);
//    final ClassTransformer transformer = new ClassTransformer();
//    transformer.setIncludePrefix(Collections.singleton(SAMPLE_CLASS_NAME));
//
//    // exercise
//    final byte[] bytes = transformer.transform(DUMMY_LOADER, SAMPLE_CLASS_NAME,
//        DUMMY_CLASS, DUMMY_DOMAIN, classAsBytes(SAMPLE_CLASS_NAME));
//    final Class<?> aClass = ClassUtils.createClass(bytes);
//    final Object sample = aClass.newInstance();
//    final Method method = aClass.getMethod("method");
//    method.invoke(sample);
//
//    // verify
//    final InvokeQueue queue = CodeCoverageStore.getInvokeQueue();
//    assertEquals(2, queue.size());
//
//    InvokeEntry invokeEntry = queue.poll();
//    assertEquals(InvokeType.METHOD, invokeEntry.getType());
//    assertEquals(0, invokeEntry.getClassId());
//    assertEquals(0, invokeEntry.getCodeId());
//
//    invokeEntry = queue.poll();
//    assertEquals(InvokeType.METHOD, invokeEntry.getType());
//    assertEquals(0, invokeEntry.getClassId());
//    assertEquals(1, invokeEntry.getCodeId());
//  }
// }
