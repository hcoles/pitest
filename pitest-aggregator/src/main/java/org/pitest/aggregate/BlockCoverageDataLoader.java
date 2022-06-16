package org.pitest.aggregate;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.pitest.classinfo.ClassName;
import org.pitest.coverage.BlockCoverage;
import org.pitest.coverage.BlockLocation;
import org.pitest.mutationtest.engine.Location;

class BlockCoverageDataLoader extends DataLoader<BlockCoverage> {

  private static final String BLOCK      = "block";
  private static final String TESTS      = "tests";
  private static final String TEST       = "test";
  private static final String NAME       = "name";
  private static final String METHOD     = "method";
  private static final String CLASSNAME  = "classname";
  private static final String NUMBER     = "number";

  private static final String OPEN_PAREN = "(";

  BlockCoverageDataLoader(final Collection<File> filesToLoad) {
    super(filesToLoad);
  }
  
  @Override
  protected Set<BlockCoverage> mapToData(XMLEventReader doc) throws XMLStreamException {
    final Set<BlockCoverage> data = new HashSet<>();
    List<String> tests = new ArrayList<>();
    StartElement enclosingNode = null;
    BlockLocation block = null;
    while (doc.hasNext()) {
      XMLEvent next = doc.peek();
      if (next.isStartElement()) {
        enclosingNode = next.asStartElement();
        String nodeName = next.asStartElement().getName().getLocalPart();
        if (nodeName.equals(BLOCK)) {
          block = toBlockLocation(enclosingNode);
        }
        if (nodeName.equals(TESTS)) {
          tests = new ArrayList<>();
        }
        if (nodeName.equals(TEST)) {
          tests.add(getAttributeValue(enclosingNode, NAME));
        }
      }
      if (next.isEndElement()) {
        String nodeName = next.asEndElement().getName().getLocalPart();
        if (nodeName.equals(BLOCK)) {
          data.add(new BlockCoverage(block, tests.isEmpty() ? null : tests));
        }
      }
      doc.next();
    }
    return data;
  }

  private BlockLocation toBlockLocation(StartElement enclosingNode) {
    BlockLocation block;
    ClassName className = ClassName.fromString(getAttributeValue(enclosingNode, CLASSNAME));
    String method = getAttributeValue(enclosingNode, METHOD);
    String methodName = method.substring(0, method.indexOf(OPEN_PAREN));
    int blockNum = getAttributeValueAsInt(enclosingNode, NUMBER);
    String methodDesc = method.substring(method.indexOf(OPEN_PAREN));
    Location location = new Location(className, methodName, methodDesc);
    block = new BlockLocation(location, blockNum);
    return block;
  }

  private String getAttributeValue(StartElement enclosingNode, String attributeName) {
    Attribute attribute = enclosingNode.getAttributeByName(QName.valueOf(attributeName));
    return attribute == null ? null : attribute.getValue();
  }

  private int getAttributeValueAsInt(StartElement enclosingNode, String attributeName) {
    String value = getAttributeValue(enclosingNode, attributeName);
    return value == null ? 0 : Integer.parseInt(value);
  }

}
