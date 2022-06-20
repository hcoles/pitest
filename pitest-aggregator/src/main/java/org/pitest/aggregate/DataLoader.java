package org.pitest.aggregate;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

abstract class DataLoader<T> {

  private static final String CANNOT_CLOSE_ERR = "Unable to close input stream";

  private final Set<File>     filesToLoad;

  protected DataLoader(final Collection<File> filesToLoad) {
    if ((filesToLoad == null) || filesToLoad.isEmpty()) {
      throw new IllegalArgumentException("Null or empty filesToLoad");
    }

    this.filesToLoad = Collections.unmodifiableSet(new HashSet<>(filesToLoad));
  }

  public Set<T> loadData() throws ReportAggregationException {
    final Set<T> data = new HashSet<>();

    for (final File file : this.filesToLoad) {
      data.addAll(loadData(file));
    }
    return data;
  }

  protected abstract Set<T> mapToData(XMLStreamReader xr) throws XMLStreamException;

  Set<T> loadData(final File dataLocation) throws ReportAggregationException {
    if (!dataLocation.exists() || !dataLocation.isFile()) {
      throw new ReportAggregationException(dataLocation.getAbsolutePath() + " does not exist or is not a file");
    }
    try {
      return loadData(new BufferedInputStream(new FileInputStream(dataLocation)), dataLocation);
    } catch (FileNotFoundException e) {
      throw new ReportAggregationException("Could not read file: " + dataLocation.getAbsolutePath(), e);
    }
  }
  
  Set<T> loadData(final InputStream inputStream, final File dataLocation) throws ReportAggregationException {
    try {
      XMLInputFactory xif = XMLInputFactory.newInstance();
      XMLStreamReader xr = xif.createXMLStreamReader(inputStream);
      return mapToData(xr);
    } catch (final XMLStreamException e) {
      throw new ReportAggregationException("Could not parse file: " + dataLocation.getAbsolutePath(), e);
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          throw new ReportAggregationException(CANNOT_CLOSE_ERR, e);
        }
      }
    }
  }

}
