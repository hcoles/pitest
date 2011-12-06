package org.pitest.project;

import org.pitest.project.impl.DefaultProjectFileParser;

/**
 * The {@see ProjectFileParserFactory} is responsible for creating a {@see ProjectFileParser} instance based
 * on a system property.
 * <p/>
 * If the system property is not set, then the default implementation of the {@see ProjectFileParser} is used instead.
 *
 * @author Aidan Morgan
 */
public class ProjectFileParserFactory {
  /**
   * The name of the system property that is used to override the default {@see ProjectFileParser} implementation.
   */
  public static final String PARSER_PROPERTY = "projectFileParser.impl";

  /**
   * The name of the default {@see ProjectFileParser} instance to use.
   */
  public static final String DEFAULT_PARSER = DefaultProjectFileParser.class.getName();

  /**
   * Creates a new {@see ProjectFileParser} instance, based on the system property. If no system property is specified
   * then the default {@see ProjectFileParser} instance is returned.
   *
   * @return a new {@see ProjectFileParser} for parsing project files.
   * @throws ProjectFileParserException if an exception occurs instantiating the {@see ProjectFileParser} instance.
   */
  public static ProjectFileParser createParser() throws ProjectFileParserException {
    try {
      String propertyValue = System.getProperty(PARSER_PROPERTY, DEFAULT_PARSER);

      Class parserClass = Class.forName(propertyValue);
      Object instance = parserClass.newInstance();

      // if the new object isn't an instance of the ProjectFileParser interface, then throw an exception.
      if (instance instanceof ProjectFileParser) {
        return (ProjectFileParser) instance;
      } else {
        throw new ProjectFileParserException("Cannot create ProjectFileParser instance from class " + propertyValue + " as it does not implement " + ProjectFileParser.class.getName() + ".");
      }
    } catch (ClassNotFoundException e) {
      throw new ProjectFileParserException(e);
    } catch (InstantiationException e) {
      throw new ProjectFileParserException(e);
    } catch (IllegalAccessException e) {
      throw new ProjectFileParserException(e);
    }
  }
}
