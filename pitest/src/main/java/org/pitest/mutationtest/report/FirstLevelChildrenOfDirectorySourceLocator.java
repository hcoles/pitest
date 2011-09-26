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
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//
//import org.pitest.functional.Option;
//
//public class FirstLevelChildrenOfDirectorySourceLocator implements
//    SourceLocator {
//
//  private final Collection<DirectorySourceLocator> children = new ArrayList<DirectorySourceLocator>();
//
//  public FirstLevelChildrenOfDirectorySourceLocator(final File file) {
//    this.children.addAll(findFirstLevelChildren(file));
//  }
//
//  private Collection<? extends DirectorySourceLocator> findFirstLevelChildren(
//      final File file) {
//    final List<DirectorySourceLocator> dsls = new ArrayList<DirectorySourceLocator>();
//    for (final File each : file.listFiles()) {
//      if (each.isDirectory()) {
//        dsls.add(new DirectorySourceLocator(each));
//      }
//    }
//    return dsls;
//  }
//
//  public Option<Reader> locate(final String fileName) {
//    for (final DirectorySourceLocator each : this.children) {
//      final Option<Reader> maybe = each.locate(fileName);
//      if (maybe.hasSome()) {
//        return maybe;
//      }
//    }
//    return Option.none();
//  }
//
// }
