package org.pitest.controller;

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
import org.pitest.minion.Minion;
import org.pitest.mutationtest.config.TestPluginArguments;
import org.pitest.process.JavaAgent;
import org.pitest.process.LaunchOptions;
import org.pitest.process.ProcessArgs;
import org.pitest.util.Unchecked;

public class MinionFactory {
  
  String java = "java";

  private final int controllerPort;
  private final String                classPath;
  private final File                  baseDir;
  private final TestPluginArguments   pitConfig;
  private final String                mutationEngine;
  private final boolean               verbose;
  private final LaunchOptions         config;
  

  private int count;
  

  public MinionFactory(int controllerPort, 
      String classPath, 
      File baseDir,
      TestPluginArguments pitConfig,
      String mutationEngine,
      boolean verbose, 
      LaunchOptions config) {
    this.controllerPort = controllerPort;
    this.classPath = classPath;
    this.baseDir = baseDir;
    this.pitConfig = pitConfig;
    this.mutationEngine = mutationEngine;
    this.verbose = verbose;
    this.config = config;
  }


   void  requestNewMinion(MinionPool pool) {
    String name = nextName();
    
    
    final ProcessArgs args = ProcessArgs.withClassPath(this.classPath)
        .andLaunchOptions(this.config)
        .andBaseDir(this.baseDir);
        
    // TODO configure the test plugin
    
    List<String> programArgs = Arrays.asList("" + controllerPort, name, pitConfig.getTestPlugin(), mutationEngine);
    
    ProcessBuilder processBuilder = createProcessBuilder(
        args.getJavaExecutable(), args.getJvmArgs(),
        Minion.class, programArgs,
        args.getJavaAgentFinder())
        .inheritIO();

    configureProcessBuilder(processBuilder, args.getWorkingDir(),
        args.getLaunchClassPath(),
        args.getEnvironmentVariables());
    
    try {
      pool.invite(name, processBuilder.start());
    } catch (IOException e) {
      throw Unchecked.translateCheckedException(e);
    }   

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
  
  
  private synchronized String nextName() {
    return "eric" + count++;
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

    List<String> cmd = new ArrayList<>();
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

}




