package org.pitest.maven.report;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.plexus.util.FileUtils;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassFilter;
import org.pitest.classpath.ClassPath;
import org.pitest.classpath.ClassPathRoot;
import org.pitest.classpath.CodeSource;
import org.pitest.classpath.DirectoryClassPathRoot;
import org.pitest.classpath.PathFilter;
import org.pitest.classpath.ProjectClassPaths;
import org.pitest.coverage.BlockCoverage;
import org.pitest.coverage.BlockLocation;
import org.pitest.coverage.CoverageData;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.coverage.TestInfo;
import org.pitest.coverage.analysis.LineMapper;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.predicate.Predicate;
import org.pitest.functional.prelude.Prelude;
import org.pitest.help.PitHelpError;
import org.pitest.junit.JUnitTestClassIdentifier;
import org.pitest.maven.DependencyFilter;
import org.pitest.mutationtest.ClassMutationResults;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationMetaData;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationResultListener;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.SourceLocator;
import org.pitest.mutationtest.config.CompoundTestClassIdentifier;
import org.pitest.mutationtest.config.DefaultCodePathPredicate;
import org.pitest.mutationtest.config.DefaultDependencyPathPredicate;
import org.pitest.mutationtest.config.DirectoryResultOutputStrategy;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.UndatedReportDirCreationStrategy;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MethodName;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.config.Mutator;
import org.pitest.mutationtest.report.html.MutationHtmlReportListener;
import org.pitest.mutationtest.tooling.SmartSourceLocator;
import org.pitest.testapi.TestClassIdentifier;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.testng.TestNGTestClassIdentifier;
import org.pitest.util.Glob;
import org.pitest.util.ResultOutputStrategy;
import org.pitest.util.Unchecked;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Goal which aggregates the results of multiple tests into a single result.
 *
 * <p>
 * Based upon Jacoco's ReportAggregateMojo, creates a structured report (HTML,
 * XML, or CSV) from multiple projects. The report is created the all modules
 * this project includes as dependencies.
 * </p>
 *
 * <p>
 * To successfully aggregate reports, each of the individual sub-module reports
 * must have the exportLineCoverage set to <code>true</code>, and must export an
 * XML formatted report. The the developer would simply include an extra module,
 * which has all of the modules which contain reports as dependencies. That
 * "report-aggregation" module would then call this mojo to aggregate all of the
 * individual reports into a single report.
 * </p>
 *
 */
@Mojo(name = "report-aggregate", defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class PitAggregationMojo extends PitReportMojo {

  /**
   * The projects in the reactor.
   */
  @Parameter(property = "reactorProjects", readonly = true)
  private List<MavenProject> reactorProjects;

  @Override
  public String getDescription(final Locale locale) {
    return getName(locale) + " Coverage Report.";
  }

  @Override
  protected void executeReport(final Locale locale)
      throws MavenReportException {

    try {
      final Collection<MavenProject> allProjects = new HashSet<MavenProject>();
      allProjects.add(getProject());
      allProjects.addAll(findDependencies());

      final Set<MutationResult> executionData = loadExecutionData(allProjects);

      final MutationMetaData mutationMetadata = new MutationMetaData(
          new ArrayList<MutationResult>(executionData));
      final MutationResultListener listener = createHtmlListener(allProjects,
          mutationMetadata);

      listener.runStart();

      for (final ClassMutationResults mutationResults : mutationMetadata
          .toClassResults()) {
        listener.handleMutationResult(mutationResults);
      }
      listener.runEnd();
    } catch (final Exception e) {
      throw new MavenReportException(e.getMessage(), e);
    }
  }

  MutationResultListener createHtmlListener(
      final Collection<MavenProject> projects, final MutationMetaData metadata)
      throws Exception {
    final ResultOutputStrategy outputStrategy = new DirectoryResultOutputStrategy(
        getReportsDirectory().getAbsolutePath(),
        new UndatedReportDirCreationStrategy());

    final SourceLocator locator = createSourceLocator(projects);
    final ClassPath path = new ClassPath(
        convertProjectsToCompiledDirs(projects));
    final CodeSource codeSource = createCodeSource(path);

    final Set<BlockCoverage> coverageData = loadCoverageData(projects);
    final CoverageDatabase coverageDatabase = calculateCoverage(coverageData,
        codeSource, metadata);
    final Collection<String> mutatorNames = FCollection.map(Mutator.all(),
        new F<MethodMutatorFactory, String>() {
          @Override
          public String apply(final MethodMutatorFactory a) {
            return a.getGloballyUniqueId();
          }
        });

    return new MutationHtmlReportListener(coverageDatabase, outputStrategy,
        mutatorNames, locator);
  }

  private CoverageData calculateCoverage(
      final Collection<BlockCoverage> coverageData, final CodeSource codeSource,
      final MutationMetaData metadata) {
    try {
      final CoverageData coverage = new CoverageData(codeSource,
          new LineMapper(codeSource));

      if (coverageData != null && !coverageData.isEmpty()) {
        final Map<BlockLocation, Set<TestInfo>> blockCoverageMap = new HashMap<BlockLocation, Set<TestInfo>>();

        for (final BlockCoverage blockData : coverageData) {
          blockCoverageMap.put(blockData.getBlock(), new HashSet<TestInfo>(
              FCollection.map(blockData.getTests(), new F<String, TestInfo>() {
                @Override
                public TestInfo apply(final String a) {
                  return new TestInfo(null, a, 0, null,
                      blockData.getBlock().getBlock());
                }
              })));

          final Field bcMap = CoverageData.class
              .getDeclaredField("blockCoverage");
          bcMap.setAccessible(true);
          bcMap.set(coverage, blockCoverageMap);
        }
      }

      return coverage;

    } catch (final PitHelpError phe) {
      throw phe;
    } catch (final Exception e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

  SourceLocator createSourceLocator(final Collection<MavenProject> projects)
      throws Exception {
    final List<File> files = convertProjectsToSourceDirs(projects);

    return new SmartSourceLocator(files);
  }

  @SuppressWarnings("unchecked")
  private List<File> convertProjectsToSourceDirs(
      final Collection<MavenProject> projects) throws Exception {
    final List<String> sourceRoots = new ArrayList<String>();
    for (final MavenProject project : projects) {
      sourceRoots.addAll(project.getCompileSourceRoots());
      sourceRoots.addAll(project.getTestCompileSourceRoots());
    }
    final List<File> files = FCollection.map(sourceRoots,
        new F<String, File>() {
          @Override
          public File apply(final String a) {
            return new File(a);
          }
        });
    return files;
  }

  @SuppressWarnings("unchecked")
  private List<File> convertProjectsToCompiledDirs(
      final Collection<MavenProject> projects) throws Exception {
    final List<String> sourceRoots = new ArrayList<String>();
    for (final MavenProject project : projects) {
      sourceRoots.addAll(project.getTestClasspathElements());
      for (final Object artifactObj : FCollection
          .filter(project.getPluginArtifactMap().values(), new DependencyFilter(
              new PluginServices(PitAggregationMojo.class.getClassLoader())))) {

        final Artifact artifact = (Artifact) artifactObj;
        sourceRoots.add(artifact.getFile().getAbsolutePath());
      }
    }
    final List<File> files = FCollection.map(sourceRoots,
        new F<String, File>() {
          @Override
          public File apply(final String a) {
            return new File(a);
          }
        });
    return files;
  }

  private Set<MutationResult> loadExecutionData(
      final Collection<MavenProject> projects) throws MavenReportException {
    final Set<MutationResult> executionData = new HashSet<MutationResult>();
    try {
      for (final MavenProject dependency : projects) {
        loadExecutionData(executionData, dependency.getBasedir());
      }

    } catch (final IOException e) {
      throw new MavenReportException("Unable to generate aggregated report", e);
    }
    return executionData;
  }

  private Set<BlockCoverage> loadCoverageData(
      final Collection<MavenProject> projects) throws MavenReportException {
    final Set<BlockCoverage> executionData = new HashSet<BlockCoverage>();
    try {
      for (final MavenProject dependency : projects) {
        loadCoverageData(executionData, dependency.getBasedir());
      }

    } catch (final IOException e) {
      throw new MavenReportException("Unable to generate aggregated report", e);
    }
    return executionData;
  }

  Collection<MutationResult> loadExecutionData(final InputStream resultXml)
      throws IOException {
    try {
      final DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance()
          .newDocumentBuilder();

      final Document doc = docBuilder.parse(resultXml);
      final Node mutationsNode = doc.getFirstChild();
      final NodeList mutationNodes = mutationsNode.getChildNodes();
      final List<MutationResult> results = new ArrayList<MutationResult>();
      for (int i = 0; i < mutationNodes.getLength(); i++) {
        final Node mutationNode = mutationNodes.item(i);
        if (mutationNode.getNodeType() == Node.ELEMENT_NODE) {
          results.add(toMutationResult(mutationNode));
        }
      }
      return results;
    } catch (final ParserConfigurationException e) {
      throw new IOException("Error parsing XML file", e);
    } catch (final SAXException e) {
      throw new IOException("Error parsing XML file", e);
    }
  }

  Collection<BlockCoverage> loadCoverageData(final InputStream resultXml)
      throws IOException {
    try {
      final DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance()
          .newDocumentBuilder();

      final Document doc = docBuilder.parse(resultXml);
      final Node coverageMainNode = doc.getFirstChild();
      final NodeList coverageNodes = coverageMainNode.getChildNodes();
      final List<BlockCoverage> results = new ArrayList<BlockCoverage>();
      for (int i = 0; i < coverageNodes.getLength(); i++) {
        final Node coverageNode = coverageNodes.item(i);
        if (coverageNode.getNodeType() == Node.ELEMENT_NODE) {
          results.add(toCoverageResult(coverageNode));
        }
      }
      return results;
    } catch (final ParserConfigurationException e) {
      throw new IOException("Error parsing XML file", e);
    } catch (final SAXException e) {
      throw new IOException("Error parsing XML file", e);
    }
  }

  @SuppressWarnings("unchecked")
  void loadExecutionData(final Set<MutationResult> results, final File baseDir)
      throws IOException {
    final List<File> files = FileUtils.getFiles(baseDir,
        "target/pit-reports/mutations.xml", "");

    for (final File file : files) {
      InputStream inputStream = null;
      try {
        inputStream = new BufferedInputStream(new FileInputStream(file));

        results.addAll(loadExecutionData(inputStream));
      } finally {
        if (inputStream != null) {
          inputStream.close();
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  void loadCoverageData(final Set<BlockCoverage> results, final File baseDir)
      throws IOException {
    final List<File> files = FileUtils.getFiles(baseDir,
        "target/pit-reports/linecoverage.xml", "");

    for (final File file : files) {
      InputStream inputStream = null;
      try {
        inputStream = new BufferedInputStream(new FileInputStream(file));

        results.addAll(loadCoverageData(inputStream));
      } finally {
        if (inputStream != null) {
          inputStream.close();
        }
      }
    }
  }

  MutationResult toMutationResult(final Node mutation) {
    final Map<String, String> map = nodeMap(mutation);

    final Location location = new Location(
        ClassName.fromString(map.get("mutatedClass")),
        MethodName.fromString(map.get("mutatedMethod")),
        map.get("methodDescription"));

    final MutationIdentifier id = new MutationIdentifier(location,
        Arrays.asList(new Integer(map.get("index"))), map.get("mutator"));

    final MutationDetails md = new MutationDetails(id, map.get("sourceFile"),
        map.get("description"), Integer.parseInt(map.get("lineNumber")),
        Integer.parseInt(map.get("block")));

    final MutationStatusTestPair status = new MutationStatusTestPair(
        Integer.parseInt(map.get("numberOfTestsRun")),
        DetectionStatus.valueOf(map.get("status")), map.get("killingTest"));

    return new MutationResult(md, status);
  }

  BlockCoverage toCoverageResult(final Node coverageNode) {
    final Map<String, String> map = nodeMap(coverageNode);

    final String method = map.get("method");
    final Location location = new Location(
        ClassName.fromString(map.get("classname")),
        MethodName.fromString(method.substring(0, method.indexOf("("))),
        method.substring(method.indexOf("(")));

    final BlockLocation blockLocation = new BlockLocation(location,
        Integer.parseInt(map.get("number")));

    final Collection<String> tests = new ArrayList<String>();

    final NodeList testsNodeList = coverageNode.getChildNodes();
    for (int i = 0; i < testsNodeList.getLength(); i++) {
      final Node testsNode = testsNodeList.item(i);
      if (testsNode.getNodeType() == Node.ELEMENT_NODE) {
        final NodeList testNodeList = testsNode.getChildNodes();
        for (int j = 0; j < testNodeList.getLength(); j++) {
          final Node testNode = testNodeList.item(j);
          if (testNode.getNodeType() == Node.ELEMENT_NODE) {
            final NamedNodeMap attrs = testNode.getAttributes();
            for (int k = 0; k < attrs.getLength(); k++) {
              final Node attr = attrs.item(k);
              final String tc = attr.getTextContent();

              if (tc != null && !tc.isEmpty()) {
                tests.add(tc);
              }
            }
          }
        }
      }
    }

    return new BlockCoverage(blockLocation, tests);
  }

  Map<String, String> nodeMap(final Node mutation) {
    final HashMap<String, String> map = new HashMap<String, String>();

    final NamedNodeMap attrs = mutation.getAttributes();
    for (int i = 0; i < attrs.getLength(); i++) {
      final Node attr = attrs.item(i);
      final String tc = attr.getTextContent();

      if (tc != null && !tc.isEmpty()) {
        map.put(attr.getNodeName(), tc);
      }
    }

    final NodeList children = mutation.getChildNodes();

    for (int i = 0; i < children.getLength(); i++) {
      final Node child = children.item(i);

      if (child.getNodeType() == Node.ELEMENT_NODE) {
        final String tc = child.getTextContent();

        if (tc != null && !tc.isEmpty()) {
          map.put(child.getNodeName(), tc);
        }
      }
    }

    return map;
  }

  CodeSource createCodeSource(final ClassPath classPath) {
    final Collection<Predicate<String>> occupiedClasses = FCollection
        .map(findOccupiedPackages(), Glob.toGlobPredicate());
    final Predicate<String> classPredicate = Prelude.or(occupiedClasses);
    final Predicate<ClassPathRoot> pathPredicate = new DefaultCodePathPredicate();
    final ClassFilter classFilter = new ClassFilter(classPredicate,
        classPredicate);
    final PathFilter pathFilter = new PathFilter(pathPredicate,
        Prelude.not(new DefaultDependencyPathPredicate()));
    final ProjectClassPaths cps = new ProjectClassPaths(classPath, classFilter,
        pathFilter);

    final List<TestClassIdentifier> identifiers = new ArrayList<TestClassIdentifier>();
    identifiers.add(new TestNGTestClassIdentifier());
    identifiers.add(new JUnitTestClassIdentifier(new TestGroupConfig(),
        new ArrayList<String>()));
    final TestClassIdentifier identifier = new CompoundTestClassIdentifier(
        identifiers);

    return new CodeSource(cps, identifier);
  }

  private Collection<String> findOccupiedPackages() {
    final Set<String> packages = new HashSet<String>();

    findOccupiedPackages(packages, getProject());
    for (final MavenProject dependency : findDependencies()) {
      findOccupiedPackages(packages, dependency);
    }
    return packages;
  }

  private void findOccupiedPackages(final Set<String> occupiedPackages,
      final MavenProject proj) {
    final String outputDirName = proj.getBuild().getOutputDirectory();
    final File outputDir = new File(outputDirName);
    if (outputDir.exists()) {
      final DirectoryClassPathRoot root = new DirectoryClassPathRoot(outputDir);
      FCollection.mapTo(root.classNames(), new F<String, String>() {
        @Override
        public String apply(final String a) {
          return ClassName.fromString(a).getPackage().asJavaName() + ".*";
        }
      }, occupiedPackages);
    }
  }

  // this method comes from
  // https://github.com/jacoco/jacoco/blob/master/jacoco-maven-plugin/src/org/jacoco/maven/ReportAggregateMojo.java
  private List<MavenProject> findDependencies() {
    final List<MavenProject> result = new ArrayList<MavenProject>();
    final List<String> scopeList = Arrays.asList(Artifact.SCOPE_COMPILE,
        Artifact.SCOPE_RUNTIME, Artifact.SCOPE_PROVIDED, Artifact.SCOPE_TEST);
    for (final Object dependencyObject : getProject().getDependencies()) {
      final Dependency dependency = (Dependency) dependencyObject;
      if (scopeList.contains(dependency.getScope())) {
        final MavenProject project = findProjectFromReactor(dependency);
        if (project != null) {
          result.add(project);
        }
      }
    }
    return result;
  }

  // this method comes from
  // https://github.com/jacoco/jacoco/blob/master/jacoco-maven-plugin/src/org/jacoco/maven/ReportAggregateMojo.java
  private MavenProject findProjectFromReactor(final Dependency d) {
    for (final MavenProject p : reactorProjects) {
      if (p.getGroupId().equals(d.getGroupId())
          && p.getArtifactId().equals(d.getArtifactId())
          && p.getVersion().equals(d.getVersion())) {
        return p;
      }
    }
    return null;
  }

}
