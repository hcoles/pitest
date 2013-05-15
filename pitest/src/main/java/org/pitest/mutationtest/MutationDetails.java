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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pitest.classinfo.ClassName;
import org.pitest.coverage.domain.TestInfo;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.instrument.ClassLine;
import org.pitest.util.StringUtil;

public class MutationDetails {

  private final MutationIdentifier  id;
  private final MethodName          method;
  private final String              filename;
  private final int                 block;
  private final int                 lineNumber;
  private final String              description;
  private final ArrayList<TestInfo> testsInOrder = new ArrayList<TestInfo>();
  private final boolean             isInFinallyBlock;

  public MutationDetails(final MutationIdentifier id, final String filename,
      final String description, final MethodName method, final int lineNumber,
      final int block) {
    this(id, filename, description, method, lineNumber, block, false);
  }

  public MutationDetails(final MutationIdentifier id, final String filename,
      final String description, final MethodName method, final int lineNumber,
      final int block, final boolean isInFinallyBlock) {
    this.id = id;
    this.description = description;
    this.method = method;
    this.filename = filename;
    this.lineNumber = lineNumber;
    this.block = block;
    this.isInFinallyBlock = isInFinallyBlock;
  }

  @Override
  public String toString() {
    return "MutationDetails [id=" + this.id + ", method=" + this.method
        + ", filename=" + this.filename + ", block=" + this.block
        + ", lineNumber=" + this.lineNumber + ", description="
        + this.description + ", testsInOrder=" + this.testsInOrder + "]";
  }

  public String getDescription() {
    return this.description;
  }

  public String getHtmlSafeDescription() {
    return StringUtil.escapeBasicHtmlChars(this.description);
  }
  
  public String getLocation() {
    return this.method.describe();
  }

  public String getClazz() {
    return this.id.getClazz();
  }

  public ClassName getClassName() {
    return this.id.getClassName();
  }

  public MethodName getMethod() {
    return this.method;
  }

  public String getFilename() {
    return this.filename;
  }

  public int getLineNumber() {
    return this.lineNumber;
  }

  public ClassLine getClassLine() {
    return new ClassLine(this.id.getClazz(), this.lineNumber);
  }

  public MutationIdentifier getId() {
    return this.id;
  }

  public List<TestInfo> getTestsInOrder() {
    return this.testsInOrder;
  }

  public void addTestsInOrder(final Collection<TestInfo> testNames) {
    this.testsInOrder.addAll(testNames);
    this.testsInOrder.trimToSize();
  }

  public boolean isInStaticInitializer() {
    return this.getMethod().raw().trim().startsWith("<clinit>");
  }

  public int getBlock() {
    return this.block;
  }

  public Boolean matchesId(final MutationIdentifier id) {
    return this.id.matches(id);
  }

  public String getMutator() {
    return this.id.getMutator();
  }

  public int getFirstIndex() {
    return this.id.getFirstIndex();
  }

  public boolean isInFinallyBlock() {
    return this.isInFinallyBlock;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.block;
    result = (prime * result)
        + ((this.description == null) ? 0 : this.description.hashCode());
    result = (prime * result)
        + ((this.filename == null) ? 0 : this.filename.hashCode());
    result = (prime * result) + ((this.id == null) ? 0 : this.id.hashCode());
    result = (prime * result) + this.lineNumber;
    result = (prime * result)
        + ((this.method == null) ? 0 : this.method.hashCode());
    result = (prime * result)
        + ((this.testsInOrder == null) ? 0 : this.testsInOrder.hashCode());
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
    if (this.block != other.block) {
      return false;
    }
    if (this.description == null) {
      if (other.description != null) {
        return false;
      }
    } else if (!this.description.equals(other.description)) {
      return false;
    }
    if (this.filename == null) {
      if (other.filename != null) {
        return false;
      }
    } else if (!this.filename.equals(other.filename)) {
      return false;
    }
    if (this.id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!this.id.equals(other.id)) {
      return false;
    }
    if (this.lineNumber != other.lineNumber) {
      return false;
    }
    if (this.method == null) {
      if (other.method != null) {
        return false;
      }
    } else if (!this.method.equals(other.method)) {
      return false;
    }
    if (this.testsInOrder == null) {
      if (other.testsInOrder != null) {
        return false;
      }
    } else if (!this.testsInOrder.equals(other.testsInOrder)) {
      return false;
    }
    return true;
  }

}
