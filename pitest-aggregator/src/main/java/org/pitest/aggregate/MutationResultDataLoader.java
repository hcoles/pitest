package org.pitest.aggregate;

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.util.Unchecked;

class MutationResultDataLoader extends DataLoader<MutationResult> {

  MutationResultDataLoader(final Collection<File> filesToLoad) {
    super(filesToLoad);
  }
  
  @Override
  protected Set<MutationResult> mapToData(XMLStreamReader xr) throws XMLStreamException {
    XmlMapper xm = new XmlMapper();
    final Set<MutationResult> data = new HashSet<>();
    while (xr.hasNext()) {
      xr.next();
      if (xr.getEventType() == START_ELEMENT) {
        if ("mutation".equals(xr.getLocalName())) {
          try {
            MutationXml mutation = xm.readValue(xr, MutationXml.class);
            data.add(xmlToResult(mutation));
          } catch (IOException e) {
            throw Unchecked.translateCheckedException(e);
          }
        }
      }
    }
    return data;
  }

  private MutationResult xmlToResult(MutationXml xml) {
    Location location = new Location(ClassName.fromString(xml.mutatedClass),
            xml.mutatedMethod, xml.methodDescription);
    MutationIdentifier id = new MutationIdentifier(location, xml.indexes, xml.mutator);
    return new MutationResult(new MutationDetails(id,
            xml.sourceFile, xml.description,
            xml.lineNumber, xml.blocks),
            new MutationStatusTestPair(xml.numberOfTestsRun, DetectionStatus.valueOf(xml.status), xml.killingTest));
  }

}
