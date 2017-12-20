package org.pitest.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.pitest.classpath.ClassPath;
import org.pitest.minion.Minion;
import org.pitest.util.Unchecked;

public class MinionFactory {
  
  String java = "java";

  private final int controllerPort;
  private int count;
  
  public MinionFactory(int controllerPort) {
    this.controllerPort = controllerPort;
  }


  void requestNewMinion(MinionPool pool) {
    String name = nextName();
    String cp = new ClassPath().getLocalClassPath();
    
    ProcessBuilder pb = new ProcessBuilder();
    //pb.command(Arrays.asList(java,"-Dcom.sun.management.jmxremote.port=" +port, "-Dcom.sun.management.jmxremote.authenticate=false","-Dcom.sun.management.jmxremote.ssl=false", Minion.class.getName()))
    pb.command(Arrays.asList(java, Minion.class.getName(), "" + controllerPort, name))
   //.redirectOutput(destination)
    .inheritIO();
    
    configureProcessBuilder(pb, cp);
    try {
      pool.invite(name, pb.start());
    } catch (IOException e) {
      throw Unchecked.translateCheckedException(e);
    }
    
   // return new MinionHandle(pb.start());
  }
  
  
  private void configureProcessBuilder(ProcessBuilder processBuilder, String initialClassPath) {
   // processBuilder.directory(workingDirectory);
    Map<String, String> environment = processBuilder.environment();
    environment.put("CLASSPATH", initialClassPath);    
      
    //for (Map.Entry<String, String> entry : environmentVariables.entrySet()) {
    //  environment.put(entry.getKey(), entry.getValue());
    //}
  }
  
  private synchronized String nextName() {
    return "eric" + count++;
  }

}
