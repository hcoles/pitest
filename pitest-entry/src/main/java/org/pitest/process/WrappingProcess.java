package org.pitest.process;

import static org.pitest.functional.prelude.Prelude.or;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.pitest.functional.FCollection;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.Option;
import org.pitest.functional.predicate.Predicate;

public class WrappingProcess {

  private final int         port;
  private final ProcessArgs processArgs;
  private final Class<?>    minionClass;

  private JavaProcess       process;

  public WrappingProcess(int port, ProcessArgs args, Class<?> minionClass) {
    this.port = port;
    this.processArgs = args;
    this.minionClass = minionClass;
  }

  public void start() throws IOException {
    final String[] args = { "" + this.port };

    ProcessBuilder processBuilder = createProcessBuilder(
        this.processArgs.getJavaExecutable(), this.processArgs.getJvmArgs(),
        this.minionClass, Arrays.asList(args),
        this.processArgs.getJavaAgentFinder());

    configureProcessBuilder(processBuilder, this.processArgs.getWorkingDir(),
        this.processArgs.getLaunchClassPath(),
        this.processArgs.getEnvironmentVariables());

    Process process = processBuilder.start();
    this.process = new JavaProcess(process, this.processArgs.getStdout(),
        this.processArgs.getStdErr());
  }

  private void configureProcessBuilder(ProcessBuilder processBuilder,
      File workingDirectory, String initialClassPath,
      Map<String, String> environmentVariables) {
    processBuilder.directory(workingDirectory);
    Map<String, String> environment = processBuilder.environment();
    environment.put("CLASSPATH", initialClassPath);
    
    for (Map.Entry<String, String> entry : environmentVariables.entrySet()) {
      environment.put(entry.getKey(), entry.getValue());
    }
  }

  public void destroy() {
    this.process.destroy();
  }

  private static ProcessBuilder createProcessBuilder(String javaProc,
      List<String> args, Class<?> mainClass, List<String> programArgs,
      JavaAgent javaAgent) {
    List<String> cmd = createLaunchArgs(javaProc, javaAgent, args, mainClass,
        programArgs);

    // IBM jdk adds this, thereby breaking everything
    removeClassPathProperties(cmd);

    return new ProcessBuilder(cmd);
  }

  private static void removeClassPathProperties(List<String> cmd) {
    for (int i = cmd.size() - 1; i >= 0; i--) {
      if (cmd.get(i).startsWith("-Djava.class.path")) {
        cmd.remove(i);
      }
    }
  }

  private static List<String> createLaunchArgs(String javaProcess,
      JavaAgent agentJarLocator, List<String> args, Class<?> mainClass,
      List<String> programArgs) {

    List<String> cmd = new ArrayList<String>();
    cmd.add(javaProcess);
    cmd.addAll(args);

    addPITJavaAgent(agentJarLocator, cmd);
    addLaunchJavaAgents(cmd);

    cmd.add(mainClass.getName());
    cmd.addAll(programArgs);
    return cmd;
  }

  private static void addPITJavaAgent(JavaAgent agentJarLocator,
      List<String> cmd) {
    Option<String> jarLocation = agentJarLocator.getJarLocation();
    for (String each : jarLocation) {
      cmd.add("-javaagent:" + each);
    }
  }

  private static void addLaunchJavaAgents(List<String> cmd) {
    RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();
    @SuppressWarnings("unchecked")
    FunctionalList<String> agents = FCollection.filter(rt.getInputArguments(),
        or(isJavaAgentParam(), isEnvironmentSetting()));
    cmd.addAll(agents);
  }

  private static Predicate<String> isEnvironmentSetting() {
    return new Predicate<String>() {
      @Override
      public Boolean apply(String a) {
        return a.startsWith("-D");
      }
    };
  }

  private static Predicate<String> isJavaAgentParam() {
    return new Predicate<String>() {
      @Override
      public Boolean apply(String a) {
        return a.toLowerCase().startsWith("-javaagent");
      }
    };
  }

  public JavaProcess getProcess() {
    return this.process;
  }
}
