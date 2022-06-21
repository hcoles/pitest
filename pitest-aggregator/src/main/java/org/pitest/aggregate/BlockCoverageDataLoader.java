package org.pitest.aggregate;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.pitest.classinfo.ClassName;
import org.pitest.coverage.BlockCoverage;
import org.pitest.coverage.BlockLocation;
import org.pitest.mutationtest.engine.Location;
import org.pitest.util.Unchecked;

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

class BlockCoverageDataLoader extends DataLoader<BlockCoverage> {

  private static final String OPEN_PAREN = "(";

  BlockCoverageDataLoader(final Collection<File> filesToLoad) {
    super(filesToLoad);
  }

  @Override
  protected Set<BlockCoverage> mapToData(XMLStreamReader xr) throws XMLStreamException {
    XmlMapper xm = new XmlMapper();
    final Set<BlockCoverage> data = new HashSet<>();
    while (xr.hasNext()) {
      xr.next();
      if (xr.getEventType() == START_ELEMENT) {
        if ("block".equals(xr.getLocalName())) {
          try {
            CoverageXml line = xm.readValue(xr, CoverageXml.class);
            data.add(xmlToCoverage(line));
          } catch (IOException e) {
            throw Unchecked.translateCheckedException(e);
          }
        }
      }
    }
    return data;
  }

  private BlockCoverage xmlToCoverage(CoverageXml line) {
    ClassName className = ClassName.fromString(line.classname);
    String methodName = line.method.substring(0, line.method.indexOf(OPEN_PAREN));
    String methodDesc = line.method.substring(line.method.indexOf(OPEN_PAREN));
    Location location = new Location(className, methodName, methodDesc);
    BlockLocation loc = new BlockLocation(location, line.number);
    return new BlockCoverage(loc, toTestStrings(line));
  }

  private List<String> toTestStrings(CoverageXml line) {
    if (line.tests == null) {
      return Collections.emptyList();
    }
    return line.tests.stream()
            .map(t -> t.name)
            .collect(Collectors.toList());
  }

}
