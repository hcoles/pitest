package org.pitest.project.impl;

import static org.pitest.mutationtest.config.ConfigOption.CLASSPATH;
import static org.pitest.mutationtest.config.ConfigOption.DEPENDENCY_DISTANCE;
import static org.pitest.mutationtest.config.ConfigOption.EXCLUDED_CLASSES;
import static org.pitest.mutationtest.config.ConfigOption.EXCLUDED_METHOD;
import static org.pitest.mutationtest.config.ConfigOption.IN_SCOPE_CLASSES;
import static org.pitest.mutationtest.config.ConfigOption.MAX_MUTATIONS_PER_CLASS;
import static org.pitest.mutationtest.config.ConfigOption.MUTATE_STATIC_INITIALIZERS;
import static org.pitest.mutationtest.config.ConfigOption.REPORT_DIR;
import static org.pitest.mutationtest.config.ConfigOption.SOURCE_DIR;
import static org.pitest.mutationtest.config.ConfigOption.TARGET_CLASSES;
import static org.pitest.mutationtest.config.ConfigOption.TEST_FILTER;
import static org.pitest.mutationtest.config.ConfigOption.THREADS;
import static org.pitest.mutationtest.config.ConfigOption.TIMEOUT_CONST;
import static org.pitest.mutationtest.config.ConfigOption.TIMEOUT_FACTOR;
import static org.pitest.mutationtest.config.ConfigOption.VERBOSE;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.pitest.functional.FCollection;
import org.pitest.functional.predicate.Predicate;
import org.pitest.mutationtest.ReportOptions;
import org.pitest.mutationtest.config.ConfigOption;
import org.pitest.project.ProjectConfigurationException;
import org.pitest.project.ProjectConfigurationParser;
import org.pitest.project.ProjectConfigurationParserException;
import org.pitest.util.Glob;
import org.pitest.util.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The default implementation of the {@see ProjectFileParser} interface. Uses a
 * simple XML document to store all of the configuration information for a
 * coverage report.
 * 
 * @author Aidan Morgan
 */
public class DefaultProjectConfigurationParser implements
    ProjectConfigurationParser {

  /**
   * The name of the filter element.
   */
  public static final String       FILTER_ELEMENT_NAME           = "filter";

  /**
   * The name of the name attribute.
   */
  public static final String       NAME_ATTRIBUTE_NAME           = "name";
  public static final String       DIRECTORY_ELEMENT_NAME        = "dir";
  public static final String       PROPERTY_ELEMENT_NAME         = "property";
  public static final String       PROPERTY_NAME_ATTRIBUTE_NAME  = "name";
  public static final String       PROPERTY_VALUE_ATTRIBUTE_NAME = "value";
  public static final String       JARFILE_ELEMENT_NAME          = "jar";
  public static final String       DOCUMENT_ROOT_ELEMENT_NAME    = "project";

  private final static Logger      LOG                           = Log
                                                                     .getLogger();

  /**
   * The {@see FileSystemDelegate} instance to use to query the file system.
   * Allows any access to the file system to be replaced if needed.
   */
  private final FileSystemDelegate fileSystemDelegate;

  /**
   * Helper method that will load all {@see FILTER_ELEMENT_NAME} elements from
   * the provided {@see Element} which are children of the {@see root} {@see
   * Element}.
   * 
   * @param doc
   *          the {@see Document} to process.
   * @param root
   *          the name of the root {@see Element} that will contain child {@see
   *          Element}s named {@see FILTER_ELEMENT_NAME}
   * @return a {@see Collection<Predicate<String>>} which contains the values in
   *         the provided {@see Document}.
   */
  private static Collection<Predicate<String>> loadFilters(final Document doc,
      final ConfigOption root) {
    final List<String> result = new ArrayList<String>();

    final Element targetTests = XmlUtils.getChildElement(
        doc.getDocumentElement(), root.getParamName());

    if (targetTests != null) {
      final List<Element> filters = XmlUtils.getChildElements(targetTests,
          FILTER_ELEMENT_NAME);

      for (final Element e : filters) {
        final String filter = XmlUtils.getAttribute(e, NAME_ATTRIBUTE_NAME);

        if ((filter != null) && (filter.length() > 0)) {
          result.add(filter);
        }
      }
    }

    return FCollection.map(result, Glob.toGlobPredicate());
  }

  private static List<String> loadNameAttributesFromChildElements(
      final Element element, final String type) {
    final List<String> result = new ArrayList<String>();
    if (element != null) {
      final List<Element> directories = XmlUtils
          .getChildElements(element, type);

      for (final Element e : directories) {
        final String classpath = XmlUtils.getAttribute(e, NAME_ATTRIBUTE_NAME);

        if ((classpath != null) && (classpath.length() > 0)) {
          result.add(classpath);
        }
      }
    }

    return result;
  }

  /**
   * Helper method that will find the {@see PROPERTY_ELEMENT_NAME} property with
   * the provided {@see propertyName} and return the value for it.
   * <p/>
   * If no property {@see Element} can be found, then this will return null.
   * 
   * @param doc
   *          the {@see Document} to search.
   * @param propertyName
   *          the name of the {@see PROPERTY_ELEMENT_NAME} to find.
   * @return the value of the {@see PROPERTY_ELEMENT_NAME} {@see Element} with
   *         the provided name, {@code null} otherwise.
   */
  private static String findProperty(final Document doc,
      final ConfigOption propertyName) {
    final List<Element> elements = XmlUtils.getChildElements(
        doc.getDocumentElement(), PROPERTY_ELEMENT_NAME);

    for (final Element e : elements) {
      final String name = XmlUtils
          .getAttribute(e, PROPERTY_NAME_ATTRIBUTE_NAME);
      final String value = XmlUtils.getAttribute(e,
          PROPERTY_VALUE_ATTRIBUTE_NAME);

      if (name.equalsIgnoreCase(propertyName.getParamName())) {
        return value;
      }
    }

    return null;
  }

  /**
   * Loads the property with the provided name from the provided {@see Document}
   * as a boolean, if no property is specified then returns the {@see
   * defaultValue}.
   * 
   * @param doc
   *          the {@see Document} to find the {@see PROPERTY_ELEMENT_NAME}
   *          {@see Element} in.
   * @param propertyName
   *          the name of the property to find.
   * @param defaultValue
   *          the default value to use if no property can be found.
   * @return the boolean value of the property if specified, otherwise the
   *         default value.
   */
  private static boolean loadBooleanProperty(final Document doc,
      final ConfigOption propertyName) {
    final String value = findProperty(doc, propertyName);

    if (value != null) {
      return Boolean.valueOf(value);
    }

    return propertyName.getDefault(Boolean.class);
  }

  /**
   * Loads the property with the provided name from the provided {@see Document}
   * as a float, if no property is specified then returns the {@see
   * defaultValue}.
   * 
   * @param doc
   *          the {@see Document} to find the {@see PROPERTY_ELEMENT_NAME}
   *          {@see Element} in.
   * @param propertyName
   *          the name of the property to find.
   * @param defaultValue
   *          the default value to use if no property can be found.
   * @return the float value of the property if specified, otherwise the default
   *         value.
   */
  private static float loadFloatProperty(final Document doc,
      final ConfigOption propertyName) {
    final String value = findProperty(doc, propertyName);

    if (value != null) {
      try {
        return Float.valueOf(value);
      } catch (final NumberFormatException e) {
        LOG.warning("NumberFormatException thrown trying to parse [" + value
            + "] for property [" + propertyName + "] to a float.");
      }
    }

    return propertyName.getDefault(Float.class);
  }

  /**
   * Loads the property with the provided name from the provided {@see Document}
   * as a long, if no property is specified then returns the {@see defaultValue}
   * .
   * 
   * @param doc
   *          the {@see Document} to find the {@see PROPERTY_ELEMENT_NAME}
   *          {@see Element} in.
   * @param propertyName
   *          the name of the property to find.
   * @param defaultValue
   *          the default value to use if no property can be found.
   * @return the long value of the property if specified, otherwise the default
   *         value.
   */
  private static long loadLongProperty(final Document doc,
      final ConfigOption propertyName) {
    final String value = findProperty(doc, propertyName);

    if (value != null) {
      try {
        return Long.valueOf(value);
      } catch (final NumberFormatException e) {
        LOG.warning("NumberFormatException thrown trying to parse [" + value
            + "] for property [" + propertyName + "] to a long.");
      }
    }

    return propertyName.getDefault(Long.class);
  }

  /**
   * Loads the property with the provided name from the provided {@see Document}
   * as an int, if no property is specified then returns the {@see defaultValue}
   * .
   * 
   * @param doc
   *          the {@see Document} to find the {@see PROPERTY_ELEMENT_NAME}
   *          {@see Element} in.
   * @param propertyName
   *          the name of the property to find.
   * @param defaultValue
   *          the default value to use if no property can be found.
   * @return the int value of the property if specified, otherwise the default
   *         value.
   */
  private static int loadIntProperty(final Document doc,
      final ConfigOption propertyName) {
    final String value = findProperty(doc, propertyName);

    if (value != null) {
      try {
        return Integer.valueOf(value);
      } catch (final NumberFormatException e) {
        LOG.warning("NumberFormatException thrown trying to parse [" + value
            + "] for property [" + propertyName + "] to an int.");
      }
    }

    return propertyName.getDefault(Integer.class);
  }

  /**
   * Loads the property with the provided name from the provided {@see Document}
   * as a String, if no property is specified then returns the {@see
   * defaultValue}.
   * 
   * @param doc
   *          the {@see Document} to find the {@see PROPERTY_ELEMENT_NAME}
   *          {@see Element} in.
   * @param propertyName
   *          the name of the property to find.
   * @param defaultValue
   *          the default value to use if no property can be found.
   * @return the String value of the property if specified, otherwise the
   *         default value.
   */
  private static String loadStringProperty(final Document doc,
      final ConfigOption propertyName) {
    final String value = findProperty(doc, propertyName);

    if (value != null) {
      return value;
    }

    return propertyName.getDefault(String.class);
  }

  /**
   * Constructor. Creates a new {@see DefaultProjectFileParser} with a {@see
   * DefaultFileSystemDelegate} as the {@see FileSystemDelegate}.
   */
  public DefaultProjectConfigurationParser() {
    this(new DefaultFileSystemDelegate());
  }

  /**
   * Constructor.
   * 
   * @param del
   *          the {@see FileSystemDelegate} instance to use for accessing the
   *          file system.
   */
  public DefaultProjectConfigurationParser(final FileSystemDelegate del) {
    if (del == null) {
      throw new IllegalArgumentException(
          "Cannot create a new DefaultProjectConfigurationParser with a null FileSystemDelegate instance.");
    }

    this.fileSystemDelegate = del;
  }

  /**
   * @inheritDoc
   */
  public ReportOptions loadProject(final String in)
      throws ProjectConfigurationParserException, ProjectConfigurationException {
    try {

      if (!this.fileSystemDelegate.exists(in)) {
        throw new ProjectConfigurationParserException(
            "Cannot load project from file " + in + " as it does not exist.");
      }

      if (!this.fileSystemDelegate.isFile(in)) {
        throw new ProjectConfigurationParserException(
            "Cannot load project from file " + in + " as it is a directory.");
      }

      if (!this.fileSystemDelegate.canRead(in)) {
        throw new ProjectConfigurationParserException(
            "Cannot load project from file " + in + " as it cannot be read.");
      }

      final InputStream inputStream = this.fileSystemDelegate.openStream(in);

      return loadConfiguration(inputStream);
    } catch (final IOException e) {
      throw new ProjectConfigurationParserException(e);
    }
  }

  /**
   * Loads a new {@see ReportOptions} from the provided {@see InputStream}.
   * 
   * @param inputStream
   *          the {@see InputStream} to load the {@see ReportOptions} from.
   * @return a new {@see ReportOptions}, configured from the provided {@see
   *         InputStream}.
   * @throws ProjectConfigurationParserException
   * 
   * @throws ProjectConfigurationException
   */
  protected ReportOptions loadConfiguration(final InputStream inputStream)
      throws ProjectConfigurationParserException, ProjectConfigurationException {
    final Document doc = XmlUtils.parseFile(inputStream);

    if (!doc.getDocumentElement().getNodeName()
        .equalsIgnoreCase(DOCUMENT_ROOT_ELEMENT_NAME)) {
      throw new ProjectConfigurationException(
          "Project file does not start with " + DOCUMENT_ROOT_ELEMENT_NAME
              + ".");
    }

    final ReportOptions ro = new ReportOptions();
    ro.setClassesInScope(loadClassesInScope(doc));
    ro.setClassPathElements(loadClassPathElements(doc));
    ro.setDependencyAnalysisMaxDistance(loadDependencyAnalysisMaxDistance(doc));
    ro.setExcludedClasses(loadExcludedClasses(doc));
    ro.setExcludedMethods(loadExcludedMethods(doc));
    ro.setMaxMutationsPerClass(loadMaxMutationsPerClass(doc));
    ro.setMutateStaticInitializers(loadMutateStaticInitialisers(doc));
    ro.setNumberOfThreads(loadNumberOfThreads(doc));
    ro.setReportDir(loadReportDir(doc));
    ro.setSourceDirs(loadSourceDirs(doc));
    ro.setTargetClasses(loadTargetClasses(doc));
    ro.setTargetTests(loadTargetTests(doc));
    ro.setTimeoutConstant(loadTimeoutConstant(doc));
    ro.setTimeoutFactor(loadTimeoutFactor(doc));
    ro.setVerbose(loadVerbose(doc));

    return ro;
  }

  /**
   * Returns {@code true} if the provided {@see File} exists on the filesystem,
   * {@code false} otherwise.
   * 
   * @param f
   *          the {@see File} to test if it exists.
   * @return {@code true} if the provided {@see File} exists on the filesystem,
   *         {@code false} otherwise.
   */
  protected boolean doesFileExist(final String f) {
    return this.fileSystemDelegate.exists(f);
  }

  /**
   * Loads the {@see OptionsParser.VERBOSE} property from the project file.
   * 
   * @param doc
   *          the {@see Document} to load the property from.
   * @return the value of the {@see OptionsParser.VERBOSE} property from the
   *         project file, or the default value if no property is specified.
   */
  private boolean loadVerbose(final Document doc) {
    return loadBooleanProperty(doc, VERBOSE);
  }

  /**
   * Loads the {@see OptionsParser.TIMEOUT_FACTOR_ARG} property from the project
   * file.
   * 
   * @param doc
   *          the {@see Document} to load the property from.
   * @return the value of the {@see OptionsParser.TIMEOUT_FACTOR_ARG} property
   *         from the project file, or the default value if no property is
   *         specified.
   */
  private float loadTimeoutFactor(final Document doc) {
    return loadFloatProperty(doc, TIMEOUT_FACTOR);
  }

  /**
   * Loads the {@see OptionsParser.TIMEOUT_CONST_ARG} property from the project
   * file.
   * 
   * @param doc
   *          the {@see Document} to load the property from.
   * @return the value of the {@see OptionsParser.TIMEOUT_CONST_ARG} property
   *         from the project file, or the default value if no property is
   *         specified.
   */
  private long loadTimeoutConstant(final Document doc) {
    return loadLongProperty(doc, TIMEOUT_CONST);
  }

  /**
   * Loads the {@see OptionsParser.TEST_FILTER_ARGS} property from the project
   * file.
   * 
   * @param doc
   *          the {@see Document} to load the property from.
   * @return the value of the {@see OptionsParser.TEST_FILTER_ARGS} property
   *         from the project file, or empty if no property is specified.
   */
  private Collection<Predicate<String>> loadTargetTests(final Document doc) {
    return loadFilters(doc, TEST_FILTER);
  }

  /**
   * Loads the {@see OptionsParser.TARGET_CLASSES_ARG} property from the project
   * file.
   * 
   * @param doc
   *          the {@see Document} to load the property from.
   * @return the value of the {@see OptionsParser.TARGET_CLASSES_ARG} property
   *         from the project file, or empty if no property is specified.
   */
  private Collection<Predicate<String>> loadTargetClasses(final Document doc) {
    return loadFilters(doc, TARGET_CLASSES);
  }

  /**
   * Loads the {@see OptionsParser.SOURCE_DIR_ARG} property from the project
   * file.
   * 
   * @param doc
   *          the {@see Document} to load the property from.
   * @return the value of the {@see OptionsParser.SOURCE_DIR_ARG} property from
   *         the project file, or the default value if no property is specified.
   * @throws org.pitest.project.ProjectConfigurationException
   *           if a source directory is specified which does not exist on the
   *           filesystem.
   */
  private Collection<File> loadSourceDirs(final Document doc)
      throws ProjectConfigurationException {
    final List<File> result = new ArrayList<File>();

    final Element targetTests = XmlUtils.getChildElement(
        doc.getDocumentElement(), SOURCE_DIR.getParamName());

    if (targetTests != null) {
      final List<Element> directories = XmlUtils.getChildElements(targetTests,
          DIRECTORY_ELEMENT_NAME);

      for (final Element e : directories) {
        final String filter = XmlUtils.getAttribute(e, NAME_ATTRIBUTE_NAME);

        if ((filter != null) && (filter.length() > 0)) {
          if (!doesFileExist(filter)) {
            throw new ProjectConfigurationException(
                "Cannot load source directory " + filter
                    + " as it does not exist.");
          }

          result.add(new File(filter));
        }
      }
    }

    return result;
  }

  /**
   * Loads the {@see OptionsParser.REPORT_DIR_ARG} property from the project
   * file.
   * 
   * @param doc
   *          the {@see Document} to load the property from.
   * @return the value of the {@see OptionsParser.REPORT_DIR_ARG} property from
   *         the project file, if no {@see OptionsParser.REPORT_DIR_ARG} is
   *         specified then a {@see ProjectConfigurationException} is thrown.
   * @throws org.pitest.project.ProjectConfigurationException
   *           if the project file is not specified.
   */
  private String loadReportDir(final Document doc)
      throws ProjectConfigurationException {
    final String val = loadStringProperty(doc, REPORT_DIR);

    if (val == null) {
      throw new ProjectConfigurationException("A project file must have the "
          + REPORT_DIR + " attribute set.");
    }

    return val;
  }

  /**
   * Loads the {@see OptionsParser.THREADS_ARG} property from the project file.
   * 
   * @param doc
   *          the {@see Document} to load the property from.
   * @return the value of the {@see OptionsParser.THREADS_ARG} property from the
   *         project file, or the default value if no property is specified.
   */
  private int loadNumberOfThreads(final Document doc) {
    return loadIntProperty(doc, THREADS);
  }

  /**
   * Loads the {@see OptionsParser.MUTATE_STATIC_INITIALIZERS_ARG} property from
   * the project file.
   * 
   * @param doc
   *          the {@see Document} to load the property from.
   * @return the value of the {@see
   *         OptionsParser.MUTATE_STATIC_INITIALIZERS_ARG} property from the
   *         project file, or the default value if no property is specified.
   */
  private boolean loadMutateStaticInitialisers(final Document doc) {
    return loadBooleanProperty(doc, MUTATE_STATIC_INITIALIZERS);
  }

  /**
   * Loads the {@see OptionsParser.MAX_MUTATIONS_PER_CLASS_ARG} property from
   * the project file.
   * 
   * @param doc
   *          the {@see Document} to load the property from.
   * @return the value of the {@see OptionsParser.MAX_MUTATIONS_PER_CLASS_ARG}
   *         property from the project file, or the default value if no property
   *         is specified.
   */
  private int loadMaxMutationsPerClass(final Document doc) {
    return loadIntProperty(doc, MAX_MUTATIONS_PER_CLASS);
  }

  /**
   * Loads the {@see OptionsParser.EXCLUDED_METHOD_ARG} property from the
   * project file.
   * 
   * @param doc
   *          the {@see Document} to load the property from.
   * @return the value of the {@see OptionsParser.EXCLUDED_METHOD_ARG} property
   *         from the project file, or the default value if no property is
   *         specified.
   */
  private Collection<Predicate<String>> loadExcludedMethods(final Document doc) {
    return loadFilters(doc, EXCLUDED_METHOD);
  }

  /**
   * Loads the {@see OptionsParser.EXCLUDED_CLASSES_ARG} property from the
   * project file.
   * 
   * @param doc
   *          the {@see Document} to load the property from.
   * @return the value of the {@see OptionsParser.EXCLUDED_CLASSES_ARG} property
   *         from the project file, or the default value if no property is
   *         specified.
   */
  private Collection<Predicate<String>> loadExcludedClasses(final Document doc) {
    return loadFilters(doc, EXCLUDED_CLASSES);
  }

  /**
   * Loads the {@see OptionsParser.DEPENDENCY_DISTANCE_ARG} property from the
   * project file.
   * 
   * @param doc
   *          the {@see Document} to load the property from.
   * @return the value of the {@see OptionsParser.DEPENDENCY_DISTANCE_ARG}
   *         property from the project file, or the default value if no property
   *         is specified.
   */
  private int loadDependencyAnalysisMaxDistance(final Document doc) {
    return loadIntProperty(doc, DEPENDENCY_DISTANCE);
  }

  /**
   * Loads the {@see OptionsParser.CLASSPATH_ARG} property from the project
   * file.
   * 
   * @param doc
   *          the {@see Document} to load the property from.
   * @return the value of the {@see OptionsParser.CLASSPATH_ARG} property from
   *         the project file, or the default value if no property is specified.
   */
  private Collection<String> loadClassPathElements(final Document doc) {
    final Element classpathElement = getElement(doc, CLASSPATH);

    final List<String> values = new ArrayList<String>();

    values.addAll(loadNameAttributesFromChildElements(classpathElement,
        DIRECTORY_ELEMENT_NAME));
    values.addAll(loadNameAttributesFromChildElements(classpathElement,
        JARFILE_ELEMENT_NAME));

    return values;
  }

  /**
   * Loads the {@see OptionsParser.IN_SCOPE_CLASSES_ARG} property from the
   * project file.
   * 
   * @param doc
   *          the {@see Document} to load the property from.
   * @return the value of the {@see OptionsParser.IN_SCOPE_CLASSES_ARG} property
   *         from the project file, or the default value if no property is
   *         specified.
   */
  private Collection<Predicate<String>> loadClassesInScope(final Document doc) {
    return loadFilters(doc, IN_SCOPE_CLASSES);
  }

  private Element getElement(final Document doc, final ConfigOption param) {
    final Element classpathElement = XmlUtils.getChildElement(
        doc.getDocumentElement(), param.getParamName());
    return classpathElement;
  }
}
