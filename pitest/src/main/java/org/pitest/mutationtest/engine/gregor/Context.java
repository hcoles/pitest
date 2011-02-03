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
package org.pitest.mutationtest.engine.gregor;

import static org.pitest.functional.Prelude.isEqualTo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.pitest.functional.F;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.MutableList;
import org.pitest.functional.Option;
import org.pitest.mutationtest.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;

public class Context {

  private final Map<String, Integer>            mutatorIndexes          = new HashMap<String, Integer>();
  // private int index = 0;

  private int                                   lastLineNumber;

  private ClassInfo                             classInfo;
  private String                                sourceFile;

  private Option<MutationIdentifier>            target                  = Option
                                                                            .none();
  private final FunctionalList<MutationDetails> mutations               = new MutableList<MutationDetails>();
  private String                                methodName;
  private boolean                               mutationFindingDisabled = false;

  public void registerMutation(final MutationDetails details) {
    if (!this.mutationFindingDisabled) {
      this.mutations.add(details);
    }
  }

  public Option<MutationIdentifier> getTargetMutation() {
    return this.target;
  }

  public MutationIdentifier getNextMutationIdentifer(
      final Class<?> implementer, final String className) {
    final int index = getAndIncrementIndex(implementer);
    return new MutationIdentifier(className, index, implementer);
  }

  private int getAndIncrementIndex(final Class<?> implementer) {
    Integer index = this.mutatorIndexes.get(implementer.getName());
    if (index == null) {
      index = 0;
    }
    this.mutatorIndexes.put(implementer.getName(), (index + 1));
    return index;

  }

  public Collection<MutationDetails> getCollectedMutations() {
    return this.mutations;
  }

  public void registerCurrentLine(final int line) {
    this.lastLineNumber = line;
  }

  public ClassInfo getClassInfo() {
    return this.classInfo;
  }

  public String getClassName() {
    return this.classInfo.getName();
  }

  public String getFileName() {
    return this.sourceFile;
  }

  public String getMethodName() {
    return this.methodName;
  }

  public int getLineNumber() {
    return this.lastLineNumber;
  }

  public MutationIdentifier registerMutation(final Class<?> implementer,
      final String description) {
    final MutationIdentifier newId = getNextMutationIdentifer(implementer,
        getClassName().replace("/", "."));
    final MutationDetails details = new MutationDetails(newId, getFileName(),
        description, getMethodName(), getLineNumber());
    registerMutation(details);
    return newId;
  }

  public void setTargetMutation(final Option<MutationIdentifier> target) {
    this.target = target;
  }

  public FunctionalList<MutationDetails> getMutationDetails(
      final MutationIdentifier id) {
    return this.mutations.filter(hasId(id));
  }

  private F<MutationDetails, Boolean> hasId(final MutationIdentifier id) {
    return new F<MutationDetails, Boolean>() {
      public Boolean apply(final MutationDetails a) {
        return a.getId().equals(id);
      }

    };
  }

  public void registerClass(final ClassInfo classInfo) {
    this.classInfo = classInfo;
  }

  public void registerSourceFile(final String source) {
    this.sourceFile = source;
  }

  public void registerMethod(final String name) {
    this.methodName = name;
  }

  public boolean shouldMutate(final MutationIdentifier newId) {
    return getTargetMutation().contains(isEqualTo(newId));
  }

  public void disableMutations() {
    this.mutationFindingDisabled = true;
  }

  public void enableMutatations() {
    this.mutationFindingDisabled = false;
  }

}
