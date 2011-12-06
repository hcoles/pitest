package org.pitest.project.impl;

import org.pitest.project.ProjectFileParserException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Set of convenience methods that make working with {@see org.w3c.dom} XML parsing easier. There are definately better
 * XML parsing libraries out there, but this has the advantage of being bundled with the JRE.
 *
 * @author Aidan Morgan
 */
public class XmlUtils {
  /**
   * Private constructor to prevent this class being instantiated as it is a utility class.
   */
  private XmlUtils() {

  }

  /**
   * Returns the value of the {@see Attribute} with the provided name for the provided {@see Elenent}, reuturns {@code null}
   * if no such {@see Attribute} exists.
   *
   * @param ele  the {@see Element} to retrieve the {@see Attribute} for.
   * @param name the name of the {@see Attribute} to retrieve.
   * @return the value of the {@see Attribute} with the provided name, {@code null} if the attribute is not specified.
   */
  public static String getAttribute(Element ele, String name) {
    String val = ele.getAttribute(name);

    if (val != null) {
      return val.trim();
    }

    return val;
  }

  /**
   * Convenience method that will open the provided {@see File} and return the {@see Document} that is contained in the file.
   *
   * @param f the {@see File} to parse.
   * @return the {@see Document} representation of the provided {@see File}.
   * @throws ProjectFileParserException if the provided {@see File} cannot be parsed.
   */
  public static Document parseFile(File f) throws ProjectFileParserException {
    try {
      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      Document doc = docBuilder.parse(new FileInputStream(f));
      return doc;
    } catch (ParserConfigurationException e) {
      throw new ProjectFileParserException(e);
    } catch (SAXException e) {
      throw new ProjectFileParserException(e);
    } catch (IOException e) {
      throw new ProjectFileParserException(e);
    }
  }

  /**
   * Returns a {@see List} of {@see Element}s which are all of the child {@see Element}s of the provided
   * {@see Elenment} with the provided name.
   * <p/>
   * If no child {@see Element}s exist with the provided name then an empty {@see List} is returned.
   *
   * @param parent      the {@see Element} to look for children of.
   * @param elementName the name of the child {@see Element}s to look for.
   * @return a {@see List} of {@see Element}s which are the children of the provided {@see Element} with the provided
   *         {@see elementName}, empty if no {@see Elements} can be found.
   */
  public static List<Element> getChildElements(Element parent, String elementName) {
    List<Element> vals = new ArrayList<Element>();

    NodeList logEntries = parent.getElementsByTagName(elementName);

    for (int i = 0; i < logEntries.getLength(); i++) {
      Element logEntryElement = (Element) logEntries.item(i);
      vals.add(logEntryElement);
    }

    return vals;
  }

  /**
   * Returns the child {@see Element} of the provided {@see Element} with the provided {@see elementName}.
   * <p/>
   * If no child {@see Element}s exist with the provided name then {@code null} is returned.
   * <p/>
   * If more than one {@see Element}s exist then a {@see IllegalStateException} is thrown.
   *
   * @param parent      the {@see Element} to look for children of.
   * @param elementName the name of the child {@see Element} to look for.
   * @return the child {@see Element} of the provided {@see Element} with the provided {@see elementName}.
   */
  public static Element getChildElement(Element parent, String elementName) {
    NodeList nl = parent.getElementsByTagName(elementName);

    if (nl.getLength() > 1) {
      throw new IllegalStateException("Cannot call getChildElement with more than one child element with the provided name.");
    }

    if (nl.getLength() == 0) {
      return null;
    }

    return (Element) nl.item(0);
  }
}
