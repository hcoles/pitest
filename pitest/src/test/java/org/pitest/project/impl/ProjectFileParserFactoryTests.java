package org.pitest.project.impl;

import junit.framework.Assert;
import org.junit.Test;
import org.pitest.project.ProjectFileParser;
import org.pitest.project.ProjectFileParserException;
import org.pitest.project.ProjectFileParserFactory;

/**
 * @author Aidan Morgan
 */
public class ProjectFileParserFactoryTests {
  @Test
  public void shouldUseDefaultParserIfNoPropertySet() throws ProjectFileParserException {
    ProjectFileParser parser = ProjectFileParserFactory.createParser();

    Assert.assertTrue(parser instanceof DefaultProjectFileParser);
  }

  @Test
  public void shouldLoadSystemPropertyParser() throws ProjectFileParserException {
    System.setProperty(ProjectFileParserFactory.PARSER_PROPERTY, FakeProjectFileParser.class.getName());

    try {
      ProjectFileParser parser = ProjectFileParserFactory.createParser();
      Assert.assertTrue(parser instanceof FakeProjectFileParser);
    } finally {
      System.setProperty(ProjectFileParserFactory.PARSER_PROPERTY, ProjectFileParserFactory.DEFAULT_PARSER);
    }
  }

  @Test
  public void shouldThrowExceptionIfParserDoesNotImplementInterface() {
    System.setProperty(ProjectFileParserFactory.PARSER_PROPERTY, String.class.getName());

    try {
      ProjectFileParserFactory.createParser();
      Assert.fail("Should not be able to create a ProjectFileParser that does not implement the interface.");
    } catch (ProjectFileParserException e) {
      // expected exception
    } finally {
      System.setProperty(ProjectFileParserFactory.PARSER_PROPERTY, ProjectFileParserFactory.DEFAULT_PARSER);
    }
  }

  @Test
  public void shouldThrowExceptionIfParserClassDoesNotExist() {
    System.setProperty(ProjectFileParserFactory.PARSER_PROPERTY, "org.foo.example.DifferentProjectFileParser");

    try {
      ProjectFileParserFactory.createParser();
      Assert.fail("Should not be able to create a ProjectFileParser from a class that does not exist.");
    } catch (ProjectFileParserException e) {
      // expected exception
    } finally {
      System.setProperty(ProjectFileParserFactory.PARSER_PROPERTY, ProjectFileParserFactory.DEFAULT_PARSER);
    }
  }

  @Test
  public void shouldThrowExceptionIfParserDoesNotHaveANoArgsConstructor() {
    System.setProperty(ProjectFileParserFactory.PARSER_PROPERTY, NoDefaultConstructorProjectFileParser.class.getName());

    try {
      ProjectFileParserFactory.createParser();
      Assert.fail("Should not be able to create a ProjectFileParser for a class with no default constructor..");
    } catch (ProjectFileParserException e) {
      // expected exception
    } finally {
      System.setProperty(ProjectFileParserFactory.PARSER_PROPERTY, ProjectFileParserFactory.DEFAULT_PARSER);
    }
  }
}
