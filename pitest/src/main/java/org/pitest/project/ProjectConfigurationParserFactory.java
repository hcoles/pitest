package org.pitest.project;

import org.pitest.project.impl.DefaultProjectConfigurationParser;

/**
 * The {@see ProjectFileParserFactory} is responsible for creating a {@see
 * ProjectFileParser} instance based on a system property.
 * <p/>
 * If the system property is not set, then the default implementation of the
 * {@see ProjectFileParser} is used instead.
 * 
 * @author Aidan Morgan
 */
public class ProjectConfigurationParserFactory {
  /**
   * The name of the system property that is used to override the default {@see
   * ProjectFileParser} implementation.
   */
  public static final String PARSER_PROPERTY = "projectConfigurationParser.impl";

  /**
   * The name of the default {@see ProjectFileParser} instance to use.
   */
  public static final String DEFAULT_PARSER  = DefaultProjectConfigurationParser.class
  .getName();

  /**
   * Creates a new {@see ProjectFileParser} instance, based on the system
   * property. If no system property is specified then the default {@see
   * ProjectFileParser} instance is returned.
   * 
   * @return a new {@see ProjectFileParser} for parsing project files.
   * @throws ProjectConfigurationParserException
   *           if an exception occurs instantiating the {@see ProjectFileParser}
   *           instance.
   */
  public static ProjectConfigurationParser createParser()
  throws ProjectConfigurationParserException {
    try {
      final String propertyValue = System.getProperty(PARSER_PROPERTY,
          DEFAULT_PARSER);

      final Class<?> parserClass = Class.forName(propertyValue);
      final Object instance = parserClass.newInstance();

      // if the new object isn't an instance of the ProjectConfigurationParser
      // interface, then throw an exception.
      if (instance instanceof ProjectConfigurationParser) {
        return (ProjectConfigurationParser) instance;
      } else {
        throw new ProjectConfigurationParserException(
            "Cannot create ProjectConfigurationParser instance from class "
            + propertyValue + " as it does not implement "
            + ProjectConfigurationParser.class.getName() + ".");
      }
    } catch (final ClassNotFoundException e) {
      throw new ProjectConfigurationParserException(e);
    } catch (final InstantiationException e) {
      throw new ProjectConfigurationParserException(e);
    } catch (final IllegalAccessException e) {
      throw new ProjectConfigurationParserException(e);
    }
  }
}
