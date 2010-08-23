///*
// * Copyright 2010 Henry Coles
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
//package org.pitest.util;
//
//import java.io.IOException;
//import java.io.InputStream;
//
//import org.junit.Test;
//import org.pitest.functional.SideEffect1;
//import org.pitest.internal.ClassPath;
//
//public class HotSwapTest {
//
//  @Test
//  // @Ignore
//  public void testStuff() throws Exception {
//    final HotSwap hs = new HotSwap();
//    final SideEffect1<String> sout = new SideEffect1<String>() {
//
//      public void apply(final String a) {
//        System.out.print(a);
//
//      }
//
//    };
//    final JavaProcess p = hs.launchVM(Hello.class, sout);
//
//    hs.prepareInitialHotSwap(Hello.class.getName(), getBytesFor());
//    hs.resume();
//
//    p.waitToDie();
//
//  }
//
//  private byte[] getBytesFor() throws IOException {
//
//    final InputStream is = Thread
//        .currentThread()
//        .getContextClassLoader()
//        .getResourceAsStream(Hello.class.getName().replace(".", "/") + ".class");
//
//    return ClassPath.streamToByteArray(is);
//
//  }
//
// }
