package org.pitest.ant;

import java.util.HashMap;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.pitest.mutationtest.MutationCoverageReport;

public class PitestTask extends Task {

  private static final String[]     REQUIRED_OPTIONS = { "targetClasses",
      "reportDir", "sourceDir"                      };
  private final Map<String, String> options          = new HashMap<String, String>();
  private String                    classpath;

  @Override
  public void execute() throws BuildException {
    try {
      execute(new Java(this));
    } catch (final Throwable t) {
      t.printStackTrace();
    }
  }

  void execute(final Java java) {
    java.setClasspath(generateClasspath());
    java.setClassname(MutationCoverageReport.class.getCanonicalName());
    java.setFailonerror(true);
    java.setFork(true);

    checkRequiredOptions();
    for (final Map.Entry<String, String> option : this.options.entrySet()) {
      java.createArg().setValue(
          "--" + option.getKey() + "=" + option.getValue());
    }

    java.execute();
  }

  private void checkRequiredOptions() {
    for (final String requiredOption : REQUIRED_OPTIONS) {
      if (optionMissing(requiredOption)) {
        throw new BuildException("You must specify the " + requiredOption + ".");
      }
    }
  }

  private boolean optionMissing(final String option) {
    return !this.options.keySet().contains(option);
  }

  private Path generateClasspath() {
    if (this.classpath == null) {
      throw new BuildException("You must specify the classpath.");
    }

    final Object reference = getProject().getReference(this.classpath);
    if (reference != null) {
      this.classpath = reference.toString();
    }

    return new Path(getProject(), this.classpath);
  }

  public void setReportDir(final String value) {
    this.options.put("reportDir", value);
  }

  public void setInScopeClasses(final String value) {
    this.options.put("inScopeClasses", value);
  }

  public void setTargetClasses(final String value) {
    this.options.put("targetClasses", value);
  }

  public void setTargetTests(final String value) {
    this.options.put("targetTests", value);
  }

  public void setDependencyDistance(final String value) {
    this.options.put("dependencyDistance", value);
  }

  public void setThreads(final String value) {
    this.options.put("threads", value);
  }

  public void setMutateStaticInits(final String value) {
    this.options.put("mutateStaticInits", value);
  }

  public void setIncludeJarFiles(final String value) {
    this.options.put("includeJarFiles", value);
  }

  public void setMutators(final String value) {
    this.options.put("mutators", value);
  }

  public void setExcludedMethods(final String value) {
    this.options.put("excludedMethods", value);
  }

  public void setExcludedClasses(final String value) {
    this.options.put("excludedClasses", value);
  }

  public void setAvoidCallsTo(final String value) {
    this.options.put("avoidCallsTo", value);
  }

  public void setVerbose(final String value) {
    this.options.put("verbose", value);
  }

  public void setTimeoutFactor(final String value) {
    this.options.put("timeoutFactor", value);
  }

  public void setTimeoutConst(final String value) {
    this.options.put("timeoutConst", value);
  }

  public void setMaxMutationsPerClass(final String value) {
    this.options.put("maxMutationsPerClass", value);
  }

  public void setJvmArgs(final String value) {
    this.options.put("jvmArgs", value);
  }

  public void setOutputFormats(final String value) {
    this.options.put("outputFormats", value);
  }

  public void setSourceDir(final String value) {
    this.options.put("sourceDir", value);
  }

  public void setClasspath(final String classpath) {
    this.classpath = classpath;
  }

}