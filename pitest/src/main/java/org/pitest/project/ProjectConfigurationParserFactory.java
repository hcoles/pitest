package org.pitest.project;

/**
 * The ProjectConfigurationParserFactory is responsible for creating a
 * ProjectConfigurationParser instance based on a system property.
 *
 * <p>If the system property is not set, then the default implementation of the
 * ProjectConfigurationParser is used instead.</p>
 * 
 * @author Aidan Morgan
 */
public class ProjectConfigurationParserFactory {
  /**
   * The name of the system property that is used to override the default
   * ProjectConfigurationParser implementation.
   */
  public static final String PARSER_PROPERTY = "projectConfigurationParser.impl";

  /**
   * Creates a new ProjectConfigurationParser instance, based on the system
   * property. If no system property is specified then the default
   * ProjectConfigurationParser instance is returned.
   * 
   * @return a new ProjectConfigurationParser for parsing project files.
   * @throws ProjectConfigurationParserException
   *           if an exception occurs instantiating the
   *           ProjectConfigurationParser instance.
   */
  public static ProjectConfigurationParser createParser()
      throws ProjectConfigurationParserException {
    try {
      final String propertyValue = System.getProperty(PARSER_PROPERTY);

      if (propertyValue == null) {
        throw new ProjectConfigurationParserException(
            "No parser implementation set");
      }

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
