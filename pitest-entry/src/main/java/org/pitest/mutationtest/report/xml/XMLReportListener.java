/*
 * Copyright 2011 Henry Coles
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
package org.pitest.mutationtest.report.xml;

import static org.pitest.mutationtest.report.xml.Tag.block;
import static org.pitest.mutationtest.report.xml.Tag.description;
import static org.pitest.mutationtest.report.xml.Tag.index;
import static org.pitest.mutationtest.report.xml.Tag.killingTest;
import static org.pitest.mutationtest.report.xml.Tag.killingTests;
import static org.pitest.mutationtest.report.xml.Tag.lineNumber;
import static org.pitest.mutationtest.report.xml.Tag.methodDescription;
import static org.pitest.mutationtest.report.xml.Tag.mutatedClass;
import static org.pitest.mutationtest.report.xml.Tag.mutatedMethod;
import static org.pitest.mutationtest.report.xml.Tag.mutation;
import static org.pitest.mutationtest.report.xml.Tag.mutator;
import static org.pitest.mutationtest.report.xml.Tag.sourceFile;
import static org.pitest.mutationtest.report.xml.Tag.succeedingTests;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.pitest.mutationtest.ClassMutationResults;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationResultListener;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.util.ResultOutputStrategy;
import org.pitest.util.StringUtil;
import org.pitest.util.Unchecked;

enum Tag {
  mutation, sourceFile, mutatedClass, mutatedMethod, methodDescription, lineNumber, mutator, index, killingTest, killingTests, succeedingTests, description, block;
}

public class XMLReportListener implements MutationResultListener {

  public static final String MUTATION_MATRIX_TEST_SEPARATOR = "|";

  private final Writer out;
  private final boolean fullMutationMatrix;

  public XMLReportListener(final ResultOutputStrategy outputStrategy, boolean fullMutationMatrix) {
    this(outputStrategy.createWriterForFile("mutations.xml"), fullMutationMatrix);
  }

  public XMLReportListener(final Writer out, boolean fullMutationMatrix) {
    this.out = out;
    this.fullMutationMatrix = fullMutationMatrix;
  }

  private void writeResult(final ClassMutationResults metaData) {
    for (final MutationResult mutation : metaData.getMutations()) {
      writeMutationResultXML(mutation);
    }
  }

  private void writeMutationResultXML(final MutationResult result) {
    write(makeNode(makeMutationNode(result), makeMutationAttributes(result),
        mutation) + "\n");
  }

  private String makeMutationAttributes(final MutationResult result) {
    return "detected='" + result.getStatus().isDetected() + "' status='"
        + result.getStatus() + "' numberOfTestsRun='"
        + result.getNumberOfTestsRun() + "'";
  }

  private String makeMutationNode(final MutationResult mutation) {
    final MutationDetails details = mutation.getDetails();
    return makeNode(clean(details.getFilename()), sourceFile)
        + makeNode(clean(details.getClassName().asJavaName()), mutatedClass)
        + makeNode(clean(details.getMethod().name()), mutatedMethod)
        + makeNode(clean(details.getId().getLocation().getMethodDesc()),
            methodDescription)
        + makeNode("" + details.getLineNumber(), lineNumber)
        + makeNode(clean(details.getMutator()), mutator)
        + makeNode("" + details.getFirstIndex(), index)
        + makeNode("" + details.getBlock(), block)
        + makeNodeWhenConditionSatisfied(!fullMutationMatrix,
            createKillingTestDesc(mutation.getKillingTest()), killingTest)
        + makeNodeWhenConditionSatisfied(fullMutationMatrix,
            createTestDesc(mutation.getKillingTests()), killingTests)
        + makeNodeWhenConditionSatisfied(fullMutationMatrix,
            createTestDesc(mutation.getSucceedingTests()), succeedingTests)
        + makeNode(clean(details.getDescription()), description);
  }

  private String clean(final String value) {
    return StringUtil.escapeBasicHtmlChars(value);
  }

  private String makeNode(final String value, final String attributes,
      final Tag tag) {
    if (value != null) {
      return "<" + tag + " " + attributes + ">" + value + "</" + tag + ">";
    } else {
      return "<" + tag + attributes + "/>";
    }

  }

  private String makeNodeWhenConditionSatisfied(final boolean condition, final String value,
      final Tag tag) {
    if (!condition) {
      return "";
    }
    
    return makeNode(value, tag);
  }

  private String makeNode(final String value, final Tag tag) {
    if (value != null) {
      return "<" + tag + ">" + value + "</" + tag + ">";
    } else {
      return "<" + tag + "/>";
    }
  }

  private String createKillingTestDesc(final Optional<String> killingTest) {
    if (killingTest.isPresent()) {
      return createTestDesc(Arrays.asList(killingTest.get()));
    } else {
      return null;
    }
  }
  
  private String createTestDesc(final List<String> tests) {
    if (tests.isEmpty()) {
      return "";
    }
    
    StringBuilder builder = new StringBuilder();
    
    for (String test : tests) {
      builder.append(test);
      builder.append(MUTATION_MATRIX_TEST_SEPARATOR);
    }
    
    // remove last separator
    builder.setLength(builder.length() - 1);
    
    return clean(builder.toString());
  }

  private void write(final String value) {
    try {
      this.out.write(value);
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

  @Override
  public void runStart() {
    write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    write("<mutations>\n");
  }

  @Override
  public void handleMutationResult(final ClassMutationResults metaData) {
    writeResult(metaData);
  }

  @Override
  public void runEnd() {
    try {
      write("</mutations>\n");
      this.out.close();
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

}
