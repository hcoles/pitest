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

import org.pitest.coverage.domain.TestInfo;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.instrument.ClassLine;
import org.pitest.util.StringUtil;

public class MutationDetails {

  private final MutationIdentifier id;
  private final String             method;
  private final String             filename;
  private final int                block;
  private final int                lineNumber;
  private final String             description;
  private final ArrayList<TestInfo>     testsInOrder = new ArrayList<TestInfo>();
  private final boolean isInFinallyBlock;

  public MutationDetails(final MutationIdentifier id, final String filename,
      final String description, final String method, final int lineNumber,
      final int block) {
    this(id, filename, description, method, lineNumber, block, false);
  }
  
  public MutationDetails(final MutationIdentifier id, final String filename,
      final String description, final String method, final int lineNumber,
      final int block, boolean isInFinallyBlock) {
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
    return "MutationDetails [id=" + id + ", method=" + method + ", filename="
        + filename + ", block=" + block + ", lineNumber=" + lineNumber
        + ", description=" + description + ", testsInOrder=" + testsInOrder
        + "]";
  }



  public String getDescription() {
    return this.description;
  }

  public String getHtmlSafeDescription() {
    return StringUtil.escapeBasicHtmlChars(this.description);
  }

  public String getClazz() {
    return this.id.getClazz();
  }

  public String getJVMClassName() {
    return this.id.getClazz().replace(".", "/");
  }

  public String getMethod() {
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
    return this.getMethod().trim().startsWith("<clinit>");
  }

  public int getBlock() {
    return this.block;
  }

  public Boolean matchesId(MutationIdentifier id) {
    return this.id.matches(id);
  }
  
  public String getMutator() {
    return id.getMutator();
  }
  
  public int getFirstIndex() {
    return id.getFirstIndex();
  }
  
  public boolean isInFinallyBlock() {
    return this.isInFinallyBlock;
  }

  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + block;
    result = prime * result
        + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((filename == null) ? 0 : filename.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + lineNumber;
    result = prime * result + ((method == null) ? 0 : method.hashCode());
    result = prime * result
        + ((testsInOrder == null) ? 0 : testsInOrder.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    MutationDetails other = (MutationDetails) obj;
    if (block != other.block)
      return false;
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals(other.description))
      return false;
    if (filename == null) {
      if (other.filename != null)
        return false;
    } else if (!filename.equals(other.filename))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (lineNumber != other.lineNumber)
      return false;
    if (method == null) {
      if (other.method != null)
        return false;
    } else if (!method.equals(other.method))
      return false;
    if (testsInOrder == null) {
      if (other.testsInOrder != null)
        return false;
    } else if (!testsInOrder.equals(other.testsInOrder))
      return false;
    return true;
  }


}
