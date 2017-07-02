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
package org.pitest.mutationtest.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pitest.classinfo.ClassName;
import org.pitest.coverage.ClassLine;
import org.pitest.coverage.TestInfo;
import org.pitest.util.Preconditions;
import org.pitest.util.StringUtil;

/**
 * Captures all data relating to a mutant.
 */
public final class MutationDetails {

  private final MutationIdentifier  id;
  private final String              filename;
  private final int                 block;
  private final int                 lineNumber;
  private final String              description;
  private final ArrayList<TestInfo> testsInOrder = new ArrayList<TestInfo>();
  private final boolean             isInFinallyBlock;
  private final PoisonStatus        poison;

  public MutationDetails(final MutationIdentifier id, final String filename,
      final String description, final int lineNumber, final int block) {
    this(id, filename, description, lineNumber, block, false, PoisonStatus.NORMAL);
  }

  public MutationDetails(final MutationIdentifier id, final String filename,
      final String description, final int lineNumber, final int block,
      final boolean isInFinallyBlock, final PoisonStatus poison) {
    this.id = id;
    this.description = Preconditions.checkNotNull(description);
    this.filename = Preconditions.checkNotNull(filename);
    this.lineNumber = lineNumber;
    this.block = block;
    this.isInFinallyBlock = isInFinallyBlock;
    this.poison = poison;
  }


  
  @Override
  public String toString() {
    return "MutationDetails [id=" + id + ", filename=" + filename + ", block="
        + block + ", lineNumber=" + lineNumber + ", description=" + description
        + ", testsInOrder=" + testsInOrder + ", isInFinallyBlock="
        + isInFinallyBlock + ", poison=" + poison + "]";
  }

  public MutationDetails withDescription(String desc) {
    return new MutationDetails(id, filename, desc, lineNumber, block, isInFinallyBlock, poison);
  }

  public MutationDetails withPoisonStatus(PoisonStatus poisonStatus) {
    return new MutationDetails(id, filename, description, lineNumber, block, isInFinallyBlock, poisonStatus);
  }
  
  /**
   * Returns the human readable description of the mutation. This may be a
   * constant string or may provide more contextual information depending on the
   * mutation operator.
   * 
   * @return Human readable description of the mutation
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * Returns the mutation description with special characters escaped
   * 
   * @return Escaped description string
   */
  @Deprecated
  public String getHtmlSafeDescription() {
    // fixme this should not be here used in string template
    return StringUtil.escapeBasicHtmlChars(this.description);
  }

  /**
   * Returns the method name in which this mutation is located as a string
   * 
   * @return method name as string
   */
  @Deprecated
  public String getLocation() {
    // fixme this should not be here used in string template
    return this.id.getLocation().describe();
  }

  /**
   * Returns the class in which this mutation is located
   * 
   * @return class in which mutation is located
   */
  public ClassName getClassName() {
    return this.id.getClassName();
  }

  /**
   * Returns the class in which this mutation is located
   * 
   * @return class in which mutation is located
   */
  public MethodName getMethod() {
    return this.id.getLocation().getMethodName();
  }

  /**
   * Returns the file in which this mutation is located
   * 
   * @return file in which mutation is located
   */
  public String getFilename() {
    return this.filename;
  }

  /**
   * Returns the line number on which the mutation occurs as reported within the
   * jvm bytecode
   * 
   * @return The line number on which the mutation occurs.
   */
  public int getLineNumber() {
    return this.lineNumber;
  }

  /**
   * Returns the ClassLine in which this mutation is located
   * 
   * @return the ClassLine in which this mutation is located
   */
  public ClassLine getClassLine() {
    return new ClassLine(this.id.getClassName(), this.lineNumber);
  }

  /**
   * Returns the identified for this mutation
   * 
   * @return a MutationIdentifier
   */
  public MutationIdentifier getId() {
    return this.id;
  }

  /**
   * Returns the tests that cover this mutation in optimised order
   * 
   * @return a list of TestInfo objects
   */
  public List<TestInfo> getTestsInOrder() {
    return this.testsInOrder;
  }

  /**
   * Adds tests to the list of covering tests
   * 
   * @param testNames
   *          The tests to add
   */
  public void addTestsInOrder(final Collection<TestInfo> testNames) {
    this.testsInOrder.addAll(testNames);
    this.testsInOrder.trimToSize();
  }

  /**
   * Indicates if this mutation might poison state within the jvm (e.g affect
   * the values of static variable)
   * 
   * @return true if the mutation might poison the jvm otherwise false
   */
  public boolean mayPoisonJVM() {
    return this.poison.mayPoison();
  }

  /**
   * Indicates if this mutation is in a static initializer block
   * 
   * @return true if in a static initializer otherwise false
   */
  public boolean isInStaticInitializer() {
    return this.poison == PoisonStatus.IS_STATIC_INITIALIZER_CODE;
  }

  /**
   * Returns the basic block in which this mutation occurs. See
   * https://github.com/hcoles/pitest/issues/131 for discussion on block
   * coverage
   * 
   * @return the block within the method that this mutation is located in
   */
  public int getBlock() {
    return this.block;
  }

  /**
   * Returns true if this mutation has a matching identifier
   * 
   * @param id
   *          the MutationIdentifier to match
   * @return true if the MutationIdentifier matches otherwise false
   */
  public Boolean matchesId(final MutationIdentifier id) {
    return this.id.matches(id);
  }

  /**
   * Returns the name of the mutator that created this mutation
   * 
   * @return the mutator name
   */
  public String getMutator() {
    return this.id.getMutator();
  }

  /**
   * Returns the index to the first instruction on which this mutation occurs.
   * This index is specific to how ASM represents the bytecode.
   *
   * @return the zero based index to the instruction
   */
  public int getFirstIndex() {
    return this.id.getFirstIndex();
  }
  
  /**
   * Zero based index to first affected ASM instruction
   * @return
   */
  public int getInstructionIndex() {
    return getFirstIndex() - 1;
  }

  /**
   * Indicates if the mutation is within a finally block
   * 
   * @return true if in finally block otherwise false
   */
  public boolean isInFinallyBlock() {
    return this.isInFinallyBlock;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((this.id == null) ? 0 : this.id.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final MutationDetails other = (MutationDetails) obj;
    if (this.id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!this.id.equals(other.id)) {
      return false;
    }
    return true;
  }


}
