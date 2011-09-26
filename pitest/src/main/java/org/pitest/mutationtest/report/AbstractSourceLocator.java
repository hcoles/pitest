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
//package org.pitest.mutationtest.report;
//
//import java.io.File;
//import java.io.Reader;
//
//import org.pitest.functional.Option;
//
//public abstract class AbstractSourceLocator implements SourceLocator {
//
//  public static String guessFileName(final Class<?> clazz) {
//    if (clazz.isMemberClass()) {
//      return guessFileName(clazz.getDeclaringClass());
//    }
//
//    if (clazz.isLocalClass()) {
//      return guessFileName(clazz.getEnclosingClass());
//    }
//
//    return classToSourceFileName(clazz);
//  }
//
//  public static String classToSourceFileName(final Class<?> clazz) {
//    return clazz.getName().replace(".", File.separator) + ".java";
//  }
//
//  public Option<Reader> locate(final Class<?> clazz) {
//    return locate(guessFileName(clazz));
//  }
//
// // protected abstract Option<Reader> locate(String fileName);
//
// }
