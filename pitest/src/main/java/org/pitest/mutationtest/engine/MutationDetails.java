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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.pitest.classinfo.ClassName;
import org.pitest.coverage.ClassLine;
import org.pitest.coverage.TestInfo;
import org.pitest.util.StringUtil;

import static java.util.Collections.singletonList;

/**
 * Captures all data relating to a mutant.
 */
public final class MutationDetails implements Serializable {

  private static final long serialVersionUID = 1L;

  private final MutationIdentifier  id;
  private final String              filename;
  private final List<Integer>       blocks;
  private final int                 lineNumber;
  private final String              description;
  private final ArrayList<TestInfo> testsInOrder = new ArrayList<>();

  public MutationDetails(final MutationIdentifier id, final String filename,
      final String description, final int lineNumber, final int block) {
    this(id, filename, description, lineNumber, singletonList(block));
  }

  public MutationDetails(final MutationIdentifier id, final String filename,
                         final String description, final int lineNumber, List<Integer> blocks) {
    this.id = id;
    this.description = Objects.requireNonNull(description);
    this.filename = defaultFilenameIfNotSupplied(filename);
    this.lineNumber = lineNumber;
    this.blocks = blocks;
  }

  @Override
  public String toString() {
    return "MutationDetails [id=" + this.id + ", filename=" + this.filename + ", block="
        + this.blocks + ", lineNumber=" + this.lineNumber + ", description=" + this.description
        + ", testsInOrder=" + this.testsInOrder + "]";
  }

  public MutationDetails withDescription(String desc) {
    return new MutationDetails(this.id, this.filename, desc, this.lineNumber, this.blocks);
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
    return StringUtil.escapeBasicHtmlChars(this.id.getLocation().describe());
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
  public String getMethod() {
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
   * Returns the basic blocks in which this mutation occurs. See
   * https://github.com/hcoles/pitest/issues/131 for discussion on block
   * coverage
   *
   * Usually this will be only a single block, but where mutants have
   * been combined as code has been inlined, the mutant may occupy
   * more than one block.
   *
   * @return the block within the method that this mutation is located in
   */
  public List<Integer> getBlocks() {
    return this.blocks;
  }

  /**
   * Legacy method to retain interface for report code expecting a single block
   */
  @Deprecated
  public int getFirstBlock() {
    return this.blocks.get(0);
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


  private String defaultFilenameIfNotSupplied(String filename) {
    // the BuildVerifier should throw an error if classes are compiled 
    // without filename debug info, however classes may be generated
    // by annotation processors. These can be dealt with based on their
    // bytecode, but they must have a non null source file. 
    if (filename == null) {
      return "unknown_source";
    }
    return filename;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final MutationDetails other = (MutationDetails) obj;
    return Objects.equals(id, other.id);
  }
}
