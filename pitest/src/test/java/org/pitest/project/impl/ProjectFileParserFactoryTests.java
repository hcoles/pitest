package org.pitest.project.impl;

import junit.framework.Assert;
import org.junit.Test;
import org.pitest.project.ProjectConfigurationParser;
import org.pitest.project.ProjectConfigurationParserException;
import org.pitest.project.ProjectConfigurationParserFactory;

/**
 * @author Aidan Morgan
 */
public class ProjectFileParserFactoryTests {
  @Test
  public void shouldUseDefaultParserIfNoPropertySet() throws ProjectConfigurationParserException {
    ProjectConfigurationParser parser = ProjectConfigurationParserFactory.createParser();

    Assert.assertTrue(parser instanceof DefaultProjectConfigurationParser);
  }

  @Test
  public void shouldLoadSystemPropertyParser() throws ProjectConfigurationParserException {
    System.setProperty(ProjectConfigurationParserFactory.PARSER_PROPERTY, FakeProjectConfigurationParser.class.getName());

    try {
      ProjectConfigurationParser parser = ProjectConfigurationParserFactory.createParser();
      Assert.assertTrue(parser instanceof FakeProjectConfigurationParser);
    } finally {
      System.setProperty(ProjectConfigurationParserFactory.PARSER_PROPERTY, ProjectConfigurationParserFactory.DEFAULT_PARSER);
    }
  }

  @Test
  public void shouldThrowExceptionIfParserDoesNotImplementInterface() {
    System.setProperty(ProjectConfigurationParserFactory.PARSER_PROPERTY, String.class.getName());

    try {
      ProjectConfigurationParserFactory.createParser();
      Assert.fail("Should not be able to create a ProjectConfigurationParser that does not implement the interface.");
    } catch (ProjectConfigurationParserException e) {
      // expected exception
    } finally {
      System.setProperty(ProjectConfigurationParserFactory.PARSER_PROPERTY, ProjectConfigurationParserFactory.DEFAULT_PARSER);
    }
  }

  @Test
  public void shouldThrowExceptionIfParserClassDoesNotExist() {
    System.setProperty(ProjectConfigurationParserFactory.PARSER_PROPERTY, "org.foo.example.DifferentProjectFileParser");

    try {
      ProjectConfigurationParserFactory.createParser();
      Assert.fail("Should not be able to create a ProjectConfigurationParser from a class that does not exist.");
    } catch (ProjectConfigurationParserException e) {
      // expected exception
    } finally {
      System.setProperty(ProjectConfigurationParserFactory.PARSER_PROPERTY, ProjectConfigurationParserFactory.DEFAULT_PARSER);
    }
  }

  @Test
  public void shouldThrowExceptionIfParserDoesNotHaveANoArgsConstructor() {
    System.setProperty(ProjectConfigurationParserFactory.PARSER_PROPERTY, NoDefaultConstructorProjectConfigurationParser.class.getName());

    try {
      ProjectConfigurationParserFactory.createParser();
      Assert.fail("Should not be able to create a ProjectConfigurationParser for a class with no default constructor..");
    } catch (ProjectConfigurationParserException e) {
      // expected exception
    } finally {
      System.setProperty(ProjectConfigurationParserFactory.PARSER_PROPERTY, ProjectConfigurationParserFactory.DEFAULT_PARSER);
    }
  }
}
