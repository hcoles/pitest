package org.pitest.project.impl;

import org.pitest.functional.FCollection;
import org.pitest.functional.predicate.Predicate;
import org.pitest.mutationtest.ReportOptions;
import org.pitest.mutationtest.commandline.OptionsParser;
import org.pitest.mutationtest.instrument.PercentAndConstantTimeoutStrategy;
import org.pitest.project.ProjectConfigurationException;
import org.pitest.project.ProjectFileParser;
import org.pitest.project.ProjectFileParserException;
import org.pitest.util.Glob;
import org.pitest.util.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * The default implementation of the {@see ProjectFileParser} interface. Uses a simple XML document to store
 * all of the configuration information for a coverage report.
 *
 * @author Aidan Morgan
 */
public class DefaultProjectFileParser implements ProjectFileParser {

  /**
   * The name of the filter element.
   */
  public static final String FILTER_ELEMENT_NAME = "filter";

  /**
   * The name of the name attribute.
   */
  public static final String NAME_ATTRIBUTE_NAME = "name";
  public static final String DIRECTORY_ELEMENT_NAME = "dir";
  public static final String PROPERTY_ELEMENT_NAME = "property";
  public static final String PROPERTY_NAME_ATTRIBUTE_NAME = "name";
  public static final String PROPERTY_VALUE_ATTRIBUTE_NAME = "value";
  public static final String JARFILE_ELEMENT_NAME = "jar";
  public static final String DOCUMENT_ROOT_ELEMENT_NAME = "project";

  private final static Logger LOG = Log.getLogger();


  /**
   * The {@see FileSystemDelegate} instance to use to query the file system. Allows any access to the
   * file system to be replaced if needed.
   */
  private FileSystemDelegate fileSystemDelegate;

  /**
   * Helper method that will load all {@see FILTER_ELEMENT_NAME} elements from the provided {@see Element}
   * which are children of the {@see root} {@see Element}.
   *
   * @param doc  the {@see Document} to process.
   * @param root the name of the root {@see Element} that will contain child {@see Element}s named {@see FILTER_ELEMENT_NAME}
   * @return a {@see Collection<Predicate<String>>} which contains the values in the provided {@see Document}.
   */
  private static Collection<Predicate<String>> loadFilters(Document doc, String root) {
    List<String> result = new ArrayList<String>();

    Element targetTests = XmlUtils.getChildElement(doc.getDocumentElement(), root);

    if (targetTests != null) {
      List<Element> filters = XmlUtils.getChildElements(targetTests, FILTER_ELEMENT_NAME);

      for (Element e : filters) {
        String filter = XmlUtils.getAttribute(e, NAME_ATTRIBUTE_NAME);

        if (filter != null && filter.length() > 0) {
          result.add(filter);
        }
      }
    }

    return FCollection.map(result, Glob.toGlobPredicate());
  }

  private static List<String> loadNameAttributesFromChildElements(Element element, String type) {
    List<String> result = new ArrayList<String>();
    if (element != null) {
      List<Element> directories = XmlUtils.getChildElements(element, type);

      for (Element e : directories) {
        String classpath = XmlUtils.getAttribute(e, NAME_ATTRIBUTE_NAME);

        if (classpath != null && classpath.length() > 0) {
          result.add(classpath);
        }
      }
    }

    return result;
  }


  /**
   * Helper method that will find the {@see PROPERTY_ELEMENT_NAME} property with the provided {@see propertyName}
   * and return the value for it.
   * <p/>
   * If no property {@see Element} can be found, then this will return null.
   *
   * @param doc          the {@see Document} to search.
   * @param propertyName the name of the {@see PROPERTY_ELEMENT_NAME} to find.
   * @return the value of the {@see PROPERTY_ELEMENT_NAME} {@see Element} with the provided name, {@code null}
   *         otherwise.
   */
  private static String findProperty(Document doc, String propertyName) {
    List<Element> elements = XmlUtils.getChildElements(doc.getDocumentElement(), PROPERTY_ELEMENT_NAME);

    for (Element e : elements) {
      String name = XmlUtils.getAttribute(e, PROPERTY_NAME_ATTRIBUTE_NAME);
      String value = XmlUtils.getAttribute(e, PROPERTY_VALUE_ATTRIBUTE_NAME);

      if (name.equalsIgnoreCase(propertyName)) {
        return value;
      }
    }

    return null;
  }

  /**
   * Loads the property with the provided name from the provided {@see Document} as a boolean, if no property
   * is specified then returns the {@see defaultValue}.
   *
   * @param doc          the {@see Document} to find the {@see PROPERTY_ELEMENT_NAME} {@see Element} in.
   * @param propertyName the name of the property to find.
   * @param defaultValue the default value to use if no property can be found.
   * @return the boolean value of the property if specified, otherwise the default value.
   */
  private static boolean loadBooleanProperty(Document doc, String propertyName, boolean defaultValue) {
    String value = findProperty(doc, propertyName);

    if (value != null) {
      return Boolean.valueOf(value);
    }

    return defaultValue;
  }

  /**
   * Loads the property with the provided name from the provided {@see Document} as a float, if no property
   * is specified then returns the {@see defaultValue}.
   *
   * @param doc          the {@see Document} to find the {@see PROPERTY_ELEMENT_NAME} {@see Element} in.
   * @param propertyName the name of the property to find.
   * @param defaultValue the default value to use if no property can be found.
   * @return the float value of the property if specified, otherwise the default value.
   */
  private static float loadFloatProperty(Document doc, String propertyName, float defaultValue) {
    String value = findProperty(doc, propertyName);

    if (value != null) {
      try {
        return Float.valueOf(value);
      } catch (NumberFormatException e) {
        LOG.warning("NumberFormatException thrown trying to parse [" + value + "] for property [" + propertyName + "] to a float.");
      }
    }

    return defaultValue;
  }

  /**
   * Loads the property with the provided name from the provided {@see Document} as a long, if no property
   * is specified then returns the {@see defaultValue}.
   *
   * @param doc          the {@see Document} to find the {@see PROPERTY_ELEMENT_NAME} {@see Element} in.
   * @param propertyName the name of the property to find.
   * @param defaultValue the default value to use if no property can be found.
   * @return the long value of the property if specified, otherwise the default value.
   */
  private static long loadLongProperty(Document doc, String propertyName, long defaultValue) {
    String value = findProperty(doc, propertyName);

    if (value != null) {
      try {
        return Long.valueOf(value);
      } catch (NumberFormatException e) {
        LOG.warning("NumberFormatException thrown trying to parse [" + value + "] for property [" + propertyName + "] to a long.");
      }
    }

    return defaultValue;
  }

  /**
   * Loads the property with the provided name from the provided {@see Document} as an int, if no property
   * is specified then returns the {@see defaultValue}.
   *
   * @param doc          the {@see Document} to find the {@see PROPERTY_ELEMENT_NAME} {@see Element} in.
   * @param propertyName the name of the property to find.
   * @param defaultValue the default value to use if no property can be found.
   * @return the int value of the property if specified, otherwise the default value.
   */
  private static int loadIntProperty(Document doc, String propertyName, int defaultValue) {
    String value = findProperty(doc, propertyName);

    if (value != null) {
      try {
        return Integer.valueOf(value);
      } catch (NumberFormatException e) {
        LOG.warning("NumberFormatException thrown trying to parse [" + value + "] for property [" + propertyName + "] to an int.");
      }
    }

    return defaultValue;
  }

  /**
   * Loads the property with the provided name from the provided {@see Document} as a String, if no property
   * is specified then returns the {@see defaultValue}.
   *
   * @param doc          the {@see Document} to find the {@see PROPERTY_ELEMENT_NAME} {@see Element} in.
   * @param propertyName the name of the property to find.
   * @param defaultValue the default value to use if no property can be found.
   * @return the String value of the property if specified, otherwise the default value.
   */
  private static String loadStringProperty(Document doc, String propertyName, String defaultValue) {
    String value = findProperty(doc, propertyName);

    if (value != null) {
      return value;
    }

    return defaultValue;
  }

  /**
   * Constructor.
   * Creates a new {@see DefaultProjectFileParser} with a {@see DefaultFileSystemDelegate} as the {@see FileSystemDelegate}.
   */
  public DefaultProjectFileParser() {
    this(new DefaultFileSystemDelegate());
  }

  /**
   * Constructor.
   *
   * @param del the {@see FileSystemDelegate} instance to use for accessing the file system.
   */
  public DefaultProjectFileParser(FileSystemDelegate del) {
    if (del == null) {
      throw new IllegalArgumentException("Cannot create a new DefaultProjectFileParser with a null FileSystemDelegate instance.");
    }

    this.fileSystemDelegate = del;
  }

  /**
   * @inheritDoc
   */
  public ReportOptions loadProjectFile(InputStream inputStream) throws ProjectFileParserException, ProjectConfigurationException {
    Document doc = XmlUtils.parseFile(inputStream);

    if (!doc.getDocumentElement().getNodeName().equalsIgnoreCase(DOCUMENT_ROOT_ELEMENT_NAME)) {
      throw new ProjectConfigurationException("Project file does not start with " + DOCUMENT_ROOT_ELEMENT_NAME + ".");
    }

    ReportOptions ro = new ReportOptions();
    ro.setClassesInScope(loadClassesInScope(doc));
    ro.setClassPathElements(loadClassPathElements(doc));
    ro.setDependencyAnalysisMaxDistance(loadDependencyAnalysisMaxDistance(doc));
    ro.setExcludedClasses(loadExcludedClasses(doc));
    ro.setExcludedMethods(loadExcludedMethods(doc));
    ro.setIncludeJarFiles(loadIncludeJarFiles(doc));
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
   * Returns {@code true} if the provided {@see File} exists on the filesystem, {@code false} otherwise.
   *
   * @param f the {@see File} to test if it exists.
   * @return {@code true} if the provided {@see File} exists on the filesystem, {@code false} otherwise.
   */
  protected boolean doesFileExist(String f) {
    return fileSystemDelegate.doesFileExist(f);
  }

  /**
   * Loads the {@see OptionsParser.VERBOSE} property from the project file.
   *
   * @param doc the {@see Document} to load the property from.
   * @return the value of the {@see OptionsParser.VERBOSE} property from the project file, or the default value
   *         if no property is specified.
   */
  private boolean loadVerbose(Document doc) {
    return loadBooleanProperty(doc, OptionsParser.VERBOSE, false);
  }

  /**
   * Loads the {@see OptionsParser.TIMEOUT_FACTOR_ARG} property from the project file.
   *
   * @param doc the {@see Document} to load the property from.
   * @return the value of the {@see OptionsParser.TIMEOUT_FACTOR_ARG} property from the project file, or the default value
   *         if no property is specified.
   */
  private float loadTimeoutFactor(Document doc) {
    return loadFloatProperty(doc, OptionsParser.TIMEOUT_FACTOR_ARG, PercentAndConstantTimeoutStrategy.DEFAULT_FACTOR);
  }

  /**
   * Loads the {@see OptionsParser.TIMEOUT_CONST_ARG} property from the project file.
   *
   * @param doc the {@see Document} to load the property from.
   * @return the value of the {@see OptionsParser.TIMEOUT_CONST_ARG} property from the project file, or the default value
   *         if no property is specified.
   */
  private long loadTimeoutConstant(Document doc) {
    return loadLongProperty(doc, OptionsParser.TIMEOUT_CONST_ARG, PercentAndConstantTimeoutStrategy.DEFAULT_CONSTANT);
  }

  /**
   * Loads the {@see OptionsParser.TEST_FILTER_ARGS} property from the project file.
   *
   * @param doc the {@see Document} to load the property from.
   * @return the value of the {@see OptionsParser.TEST_FILTER_ARGS} property from the project file, or empty
   *         if no property is specified.
   */
  private Collection<Predicate<String>> loadTargetTests(Document doc) {
    return loadFilters(doc, OptionsParser.TEST_FILTER_ARGS);
  }

  /**
   * Loads the {@see OptionsParser.TARGET_CLASSES_ARG} property from the project file.
   *
   * @param doc the {@see Document} to load the property from.
   * @return the value of the {@see OptionsParser.TARGET_CLASSES_ARG} property from the project file, or empty
   *         if no property is specified.
   */
  private Collection<Predicate<String>> loadTargetClasses(Document doc) {
    return loadFilters(doc, OptionsParser.TARGET_CLASSES_ARG);
  }

  /**
   * Loads the {@see OptionsParser.SOURCE_DIR_ARG} property from the project file.
   *
   * @param doc the {@see Document} to load the property from.
   * @return the value of the {@see OptionsParser.SOURCE_DIR_ARG} property from the project file, or the default value
   *         if no property is specified.
   * @throws org.pitest.project.ProjectConfigurationException
   *          if a source directory is specified which does not exist on
   *          the filesystem.
   */
  private Collection<File> loadSourceDirs(Document doc) throws ProjectConfigurationException {
    List<File> result = new ArrayList<File>();

    Element targetTests = XmlUtils.getChildElement(doc.getDocumentElement(), OptionsParser.SOURCE_DIR_ARG);

    if (targetTests != null) {
      List<Element> directories = XmlUtils.getChildElements(targetTests, DIRECTORY_ELEMENT_NAME);

      for (Element e : directories) {
        String filter = XmlUtils.getAttribute(e, NAME_ATTRIBUTE_NAME);

        if (filter != null && filter.length() > 0) {
          if (!doesFileExist(filter)) {
            throw new ProjectConfigurationException("Cannot load source directory " + filter + " as it does not exist.");
          }

          result.add(new File(filter));
        }
      }
    }

    return result;
  }


  /**
   * Loads the {@see OptionsParser.REPORT_DIR_ARG} property from the project file.
   *
   * @param doc the {@see Document} to load the property from.
   * @return the value of the {@see OptionsParser.REPORT_DIR_ARG} property from the project file, if no {@see OptionsParser.REPORT_DIR_ARG}
   *         is specified then a {@see ProjectConfigurationException} is thrown.
   * @throws org.pitest.project.ProjectConfigurationException
   *          if the project file is not specified.
   */
  private String loadReportDir(Document doc) throws ProjectConfigurationException {
    String val = loadStringProperty(doc, OptionsParser.REPORT_DIR_ARG, null);

    if (val == null) {
      throw new ProjectConfigurationException("A project file must have the " + OptionsParser.REPORT_DIR_ARG + " attribute set.");
    }

    return val;
  }

  /**
   * Loads the {@see OptionsParser.THREADS_ARG} property from the project file.
   *
   * @param doc the {@see Document} to load the property from.
   * @return the value of the {@see OptionsParser.THREADS_ARG} property from the project file, or the default value
   *         if no property is specified.
   */
  private int loadNumberOfThreads(Document doc) {
    return loadIntProperty(doc, OptionsParser.THREADS_ARG, 1);
  }

  /**
   * Loads the {@see OptionsParser.MUTATE_STATIC_INITIALIZERS_ARG} property from the project file.
   *
   * @param doc the {@see Document} to load the property from.
   * @return the value of the {@see OptionsParser.MUTATE_STATIC_INITIALIZERS_ARG} property from the project file, or the default value
   *         if no property is specified.
   */
  private boolean loadMutateStaticInitialisers(Document doc) {
    return loadBooleanProperty(doc, OptionsParser.MUTATE_STATIC_INITIALIZERS_ARG, false);
  }

  /**
   * Loads the {@see OptionsParser.MAX_MUTATIONS_PER_CLASS_ARG} property from the project file.
   *
   * @param doc the {@see Document} to load the property from.
   * @return the value of the {@see OptionsParser.MAX_MUTATIONS_PER_CLASS_ARG} property from the project file, or the default value
   *         if no property is specified.
   */
  private int loadMaxMutationsPerClass(Document doc) {
    return loadIntProperty(doc, OptionsParser.MAX_MUTATIONS_PER_CLASS_ARG, 0);
  }

  /**
   * Loads the {@see OptionsParser.INCLUDE_JAR_FILES} property from the project file.
   *
   * @param doc the {@see Document} to load the property from.
   * @return the value of the {@see OptionsParser.INCLUDE_JAR_FILES} property from the project file, or the default value
   *         if no property is specified.
   */
  private boolean loadIncludeJarFiles(Document doc) {
    return loadBooleanProperty(doc, OptionsParser.INCLUDE_JAR_FILES, false);
  }

  /**
   * Loads the {@see OptionsParser.EXCLUDED_METHOD_ARG} property from the project file.
   *
   * @param doc the {@see Document} to load the property from.
   * @return the value of the {@see OptionsParser.EXCLUDED_METHOD_ARG} property from the project file, or the default value
   *         if no property is specified.
   */
  private Collection<Predicate<String>> loadExcludedMethods(Document doc) {
    return loadFilters(doc, OptionsParser.EXCLUDED_METHOD_ARG);
  }

  /**
   * Loads the {@see OptionsParser.EXCLUDED_CLASSES_ARG} property from the project file.
   *
   * @param doc the {@see Document} to load the property from.
   * @return the value of the {@see OptionsParser.EXCLUDED_CLASSES_ARG} property from the project file, or the default value
   *         if no property is specified.
   */
  private Collection<Predicate<String>> loadExcludedClasses(Document doc) {
    return loadFilters(doc, OptionsParser.EXCLUDED_CLASSES_ARG);
  }

  /**
   * Loads the {@see OptionsParser.DEPENDENCY_DISTANCE_ARG} property from the project file.
   *
   * @param doc the {@see Document} to load the property from.
   * @return the value of the {@see OptionsParser.DEPENDENCY_DISTANCE_ARG} property from the project file, or the default value
   *         if no property is specified.
   */
  private int loadDependencyAnalysisMaxDistance(Document doc) {
    return loadIntProperty(doc, OptionsParser.DEPENDENCY_DISTANCE_ARG, -1);
  }

  /**
   * Loads the {@see OptionsParser.CLASSPATH_ARG} property from the project file.
   *
   * @param doc the {@see Document} to load the property from.
   * @return the value of the {@see OptionsParser.CLASSPATH_ARG} property from the project file, or the default value
   *         if no property is specified.
   */
  private Collection<String> loadClassPathElements(Document doc) {
    Element classpathElement = XmlUtils.getChildElement(doc.getDocumentElement(), OptionsParser.CLASSPATH_ARG);

    List<String> values = new ArrayList<String>();

    values.addAll(loadNameAttributesFromChildElements(classpathElement, DIRECTORY_ELEMENT_NAME));
    values.addAll(loadNameAttributesFromChildElements(classpathElement, JARFILE_ELEMENT_NAME));

    return values;
  }


  /**
   * Loads the {@see OptionsParser.IN_SCOPE_CLASSES_ARG} property from the project file.
   *
   * @param doc the {@see Document} to load the property from.
   * @return the value of the {@see OptionsParser.IN_SCOPE_CLASSES_ARG} property from the project file, or the default value
   *         if no property is specified.
   */
  private Collection<Predicate<String>> loadClassesInScope(Document doc) {
    return loadFilters(doc, OptionsParser.IN_SCOPE_CLASSES_ARG);
  }
}
