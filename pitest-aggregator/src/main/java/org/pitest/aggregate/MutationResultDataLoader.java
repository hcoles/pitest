package org.pitest.aggregate;

import static java.util.Arrays.asList;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;

class MutationResultDataLoader extends DataLoader<MutationResult> {

  private static final String MUTATION            = "mutation";
  private static final String MUTATED_CLASS       = "mutatedClass";
  private static final String MUTATED_METHOD      = "mutatedMethod";
  private static final String METHOD_DESCRIPTION  = "methodDescription";
  private static final String INDEX               = "index";
  private static final String MUTATOR             = "mutator";
  private static final String SOURCE_FILE         = "sourceFile";
  private static final String DESCRIPTION         = "description";
  private static final String LINE_NUMBER         = "lineNumber";
  private static final String BLOCK               = "block";
  private static final String NUMBER_OF_TESTS_RUN = "numberOfTestsRun";
  private static final String STATUS              = "status";
  private static final String KILLING_TEST        = "killingTest";

  MutationResultDataLoader(final Collection<File> filesToLoad) {
    super(filesToLoad);
  }
  
  @Override
  protected Set<MutationResult> mapToData(XMLEventReader doc) throws XMLStreamException {
    final Set<MutationResult> data = new HashSet<>();
    StartElement enclosingNode = null;
    XmlData xmlData = null;
    while (doc.hasNext()) {
      XMLEvent next = doc.peek();
      if (next.isStartElement()) {
        enclosingNode = next.asStartElement();
        String nodeName = next.asStartElement().getName().getLocalPart();
        if (nodeName.equals(MUTATION)) {
          xmlData = new XmlData();
          xmlData.numberOfTestsRun = Integer.parseInt(getAttributeValue(enclosingNode, NUMBER_OF_TESTS_RUN));
          xmlData.status = getAttributeValue(enclosingNode, STATUS);
        }
      }
      if (next.isCharacters() && enclosingNode != null && xmlData != null) {
        String nodeName = enclosingNode.getName().getLocalPart();
        xmlData.append(nodeName, next.asCharacters().getData());
      }
      if (next.isEndElement()) {
        enclosingNode = null;
        String nodeName = next.asEndElement().getName().getLocalPart();
        if (nodeName.equals(MUTATION)) {
          Location location = new Location(ClassName.fromString(xmlData.getMutatedClass()), 
            xmlData.getMutatedMethod(), xmlData.getMethodDescription());
          MutationIdentifier id = new MutationIdentifier(location, asList(xmlData.getIndex()), xmlData.getMutator());
          data.add(new MutationResult(new MutationDetails(id, 
            xmlData.getSourceFile(), xmlData.getDescription(), 
            xmlData.getLineNumber(), xmlData.getBlock()),
            new MutationStatusTestPair(xmlData.getNumberOfTestsRun(), xmlData.getStatus(), xmlData.getKillingTest())));
        }
      }
      doc.next();
    }
    return data;
  }
  
  private String getAttributeValue(StartElement enclosingNode, String attributeName) {
    Attribute attribute = enclosingNode.getAttributeByName(QName.valueOf(attributeName));
    return attribute == null ? null : attribute.getValue();
  }
  
  private static class XmlData {
    private int numberOfTestsRun;
    private String status;
    private StringBuffer sourceFile = new StringBuffer();
    private StringBuffer mutatedClass = new StringBuffer();
    private StringBuffer mutatedMethod = new StringBuffer();
    private StringBuffer methodDescription = new StringBuffer();
    private StringBuffer lineNumber = new StringBuffer();
    private StringBuffer mutator = new StringBuffer();
    private StringBuffer index = new StringBuffer();
    private StringBuffer killingTest = new StringBuffer();
    private StringBuffer description = new StringBuffer();
    private StringBuffer block = new StringBuffer();
    public int getNumberOfTestsRun() {
      return numberOfTestsRun;
    }
    public void append(String nodeName, String text) {
      if (nodeName.equals(SOURCE_FILE)) {
        sourceFile.append(text);
      }
      if (nodeName.equals(MUTATED_CLASS)) {
        mutatedClass.append(text);
      }
      if (nodeName.equals(MUTATED_METHOD)) {
        mutatedMethod.append(text);
      }
      if (nodeName.equals(METHOD_DESCRIPTION)) {
        methodDescription.append(text);
      }
      if (nodeName.equals(LINE_NUMBER)) {
        lineNumber.append(text);
      }
      if (nodeName.equals(MUTATOR)) {
        mutator.append(text);
      }
      if (nodeName.equals(INDEX)) {
        index.append(text);
      }
      if (nodeName.equals(KILLING_TEST)) {
        killingTest.append(text);
      }
      if (nodeName.equals(DESCRIPTION)) {
        description.append(text);
      }
      if (nodeName.equals(BLOCK)) {
        block.append(text);
      }
    }
    
    public DetectionStatus getStatus() {
      return DetectionStatus.valueOf(status);
    }
    public String getSourceFile() {
      return sourceFile.toString().trim();
    }
    public String getMutatedClass() {
      return mutatedClass.toString().trim();
    }
    public String getMutatedMethod() {
      return mutatedMethod.toString().trim();
    }
    public String getMethodDescription() {
      return methodDescription.toString().trim();
    }
    public int getLineNumber() {
      return Integer.parseInt(lineNumber.toString().trim());
    }
    public String getMutator() {
      return mutator.toString().trim();
    }
    public int getIndex() {
      return Integer.parseInt(index.toString().trim());
    }
    public String getKillingTest() {
      String str = killingTest.toString().trim();
      return str.isEmpty() ? null : str;
    }
    public String getDescription() {
      return description.toString().trim();
    }
    public int getBlock() {
      return Integer.parseInt(block.toString().trim());
    }
  }
}
