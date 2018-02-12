package org.pitest.aggregate;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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

  protected abstract T mapToData(Map<String, Object> map);

  Set<T> loadData(final File dataLocation) throws ReportAggregationException {
    if (!dataLocation.exists() || !dataLocation.isFile()) {
      throw new ReportAggregationException(dataLocation.getAbsolutePath() + " does not exist or is not a file");
    }
    final Set<T> data = new HashSet<>();
    try {
      final InputStream inputStream = new BufferedInputStream(new FileInputStream(dataLocation));

      final Document doc = readDocument(inputStream);

      final Node docNode = doc.getFirstChild();
      final NodeList nodeList = docNode.getChildNodes();
      for (int i = 0; i < nodeList.getLength(); i++) {
        final Node itemNode = nodeList.item(i);
        if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
          data.add(mapToData(nodeMap(itemNode)));
        }
      }
      return data;
    } catch (final IOException e) {
      throw new ReportAggregationException("Could not read file: " + dataLocation.getAbsolutePath(), e);
    }
  }

  /**
   * Reads the input stream into a document and closes the input stream when
   * finished.
   */
  static Document readDocument(final InputStream inputStream) throws ReportAggregationException {
    DocumentBuilder docBuilder;
    try {
      docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      return docBuilder.parse(inputStream);
    } catch (final IOException e) {
      throw new ReportAggregationException(e.getMessage(), e);
    } catch (final SAXException e) {
      throw new ReportAggregationException(e.getMessage(), e);
    } catch (final ParserConfigurationException e) {
      throw new ReportAggregationException(e.getMessage(), e);
    } finally {
      try {
        inputStream.close();
      } catch (final IOException e) {
        throw new ReportAggregationException(CANNOT_CLOSE_ERR, e);
      }
    }
  }

  // converts the contents of a node into a map
  static Map<String, Object> nodeMap(final Node node) {
    final HashMap<String, Object> map = new HashMap<>();

    final NamedNodeMap attrs = node.getAttributes();
    for (int i = 0; i < attrs.getLength(); i++) {
      final Node attr = attrs.item(i);
      final String tc = attr.getTextContent().trim();

      if (!tc.isEmpty()) {
        map.put(attr.getNodeName(), tc);
      }
    }

    final NodeList children = node.getChildNodes();

    for (int i = 0; i < children.getLength(); i++) {
      final Node child = children.item(i);

      if (child.getNodeType() == Node.ELEMENT_NODE) {
        final String tc = child.getTextContent().trim();
        if (!tc.isEmpty()) {
          map.put(child.getNodeName(), tc);
        } else {
          // may have test nodes
          final List<String> tests = new ArrayList<>();
          final NodeList testNodeList = child.getChildNodes();
          for (int j = 0; j < testNodeList.getLength(); j++) {
            final Node testNode = testNodeList.item(j);
            if (testNode.getNodeType() == Node.ELEMENT_NODE) {
              final NamedNodeMap testAttrs = testNode.getAttributes();
              for (int k = 0; k < testAttrs.getLength(); k++) {
                final Node attr = testAttrs.item(k);
                final String tn = attr.getTextContent().trim();

                if (!tn.isEmpty()) {
                  tests.add(tn);
                }
              }
            }
          }
          if (!tests.isEmpty()) {
            map.put(child.getNodeName(), tests);
          }
        }
      }
    }

    return map;
  }
}
