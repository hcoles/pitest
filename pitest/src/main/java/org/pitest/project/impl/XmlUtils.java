package org.pitest.project.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.pitest.project.ProjectConfigurationParserException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Set of convenience methods that make working with org.w3c.dom XML parsing
 * easier. There are definately better XML parsing libraries out there, but this
 * has the advantage of being bundled with the JRE.
 * 
 * @author Aidan Morgan
 */
public final class XmlUtils {
  /**
   * Private constructor to prevent this class being instantiated as it is a
   * utility class.
   */
  private XmlUtils() {

  }

  /**
   * Returns the value of the Attribute with the provided name for the provided
   * Element, returns {@code null} if no such Attribute exists.
   * 
   * @param ele
   *          the Element to retrieve the Attribute for.
   * @param name
   *          the name of the Attribute to retrieve.
   * @return the value of the Attribute with the provided name, {@code null} if
   *         the attribute is not specified.
   */
  public static String getAttribute(final Element ele, final String name) {
    final String val = ele.getAttribute(name);

    if (val != null) {
      return val.trim();
    }

    return val;
  }

  /**
   * Convenience method that will open the provided InputStream and return the
   * Document that is contained in the file.
   * 
   * @param f
   *          the InputStream to parse the XML from.
   * @return the Document representation of the provided File.
   * @throws org.pitest.project.ProjectConfigurationParserException
   *           if the provided File cannot be parsed.
   */
  public static Document parseFile(final InputStream f)
      throws ProjectConfigurationParserException {
    try {
      final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
          .newInstance();
      final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      return docBuilder.parse(f);
    } catch (final ParserConfigurationException e) {
      throw new ProjectConfigurationParserException(e);
    } catch (final SAXException e) {
      throw new ProjectConfigurationParserException(e);
    } catch (final IOException e) {
      throw new ProjectConfigurationParserException(e);
    }
  }

  /**
   * Returns a List of Elements which are all of the child Elements of the
   * provided Element with the provided name.
   * <p/>
   * If no child Elements exist with the provided name then an empty List is
   * returned.
   * 
   * @param parent
   *          the Element to look for children of.
   * @param elementName
   *          the name of the child Elements to look for.
   * @return a List of Elements which are the children of the provided Element
   *         with the provided elementName, empty if no Elements can be found.
   */
  public static List<Element> getChildElements(final Element parent,
      final String elementName) {
    final List<Element> vals = new ArrayList<Element>();

    final NodeList logEntries = parent.getElementsByTagName(elementName);

    for (int i = 0; i < logEntries.getLength(); i++) {
      final Element logEntryElement = (Element) logEntries.item(i);
      vals.add(logEntryElement);
    }

    return vals;
  }

  /**
   * Returns the child Element of the provided Element with the provided
   * elementName.
   * <p/>
   * If no child Elements exist with the provided name then {@code null} is
   * returned.
   * <p/>
   * If more than one Elements exist then a IllegalStateException is thrown.
   * 
   * @param parent
   *          the Element to look for children of.
   * @param elementName
   *          the name of the child Element to look for.
   * @return the child Element of the provided Element with the provided
   *         elementName.
   */
  public static Element getChildElement(final Element parent,
      final String elementName) {
    final NodeList nl = parent.getElementsByTagName(elementName);

    if (nl.getLength() > 1) {
      throw new IllegalStateException(
          "Cannot call getChildElement with more than one child element with the provided name.");
    }

    if (nl.getLength() == 0) {
      return null;
    }

    return (Element) nl.item(0);
  }
}
