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
import java.util.Optional;
import java.util.function.Predicate;

import org.pitest.functional.FCollection;

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

    final ProcessBuilder processBuilder = createProcessBuilder(
        this.processArgs.getJavaExecutable(), this.processArgs.getJvmArgs(),
        this.minionClass, Arrays.asList(args),
        this.processArgs.getJavaAgentFinder());

    configureProcessBuilder(processBuilder, this.processArgs.getWorkingDir(),
        this.processArgs.getLaunchClassPath(),
        this.processArgs.getEnvironmentVariables());

    final Process process = processBuilder.start();
    this.process = new JavaProcess(process, this.processArgs.getStdout(),
        this.processArgs.getStdErr());
  }

  private void configureProcessBuilder(ProcessBuilder processBuilder,
      File workingDirectory, String initialClassPath,
      Map<String, String> environmentVariables) {
    processBuilder.directory(workingDirectory);
    final Map<String, String> environment = processBuilder.environment();
    environment.put("CLASSPATH", initialClassPath);

    for (final Map.Entry<String, String> entry : environmentVariables.entrySet()) {
      environment.put(entry.getKey(), entry.getValue());
    }
  }

  public void destroy() {
    this.process.destroy();
  }

  private static ProcessBuilder createProcessBuilder(String javaProc,
      List<String> args, Class<?> mainClass, List<String> programArgs,
      JavaAgent javaAgent) {
    final List<String> cmd = createLaunchArgs(javaProc, javaAgent, args, mainClass,
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

    final List<String> cmd = new ArrayList<>();
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
    final Optional<String> jarLocation = agentJarLocator.getJarLocation();
    jarLocation.ifPresent(l -> cmd.add("-javaagent:" + l));
  }

  private static void addLaunchJavaAgents(List<String> cmd) {
    final RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();
    final List<String> agents = FCollection.filter(rt.getInputArguments(),
        or(isJavaAgentParam(), isEnvironmentSetting()));
    cmd.addAll(agents);
  }

  private static Predicate<String> isEnvironmentSetting() {
    return a -> a.startsWith("-D");
  }

  private static Predicate<String> isJavaAgentParam() {
    return a -> a.toLowerCase().startsWith("-javaagent");
  }

  public JavaProcess getProcess() {
    return this.process;
  }
}
