package org.pitest.minion;

import java.io.IOException;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.pitest.minion.commands.Command;
import org.pitest.minion.commands.Status;
import org.pitest.util.Unchecked;

public class Minion {

  public static void main(String[] args) {
    int controllerPort = Integer.parseInt(args[0]);
    String name = args[1];
    
    try {
            
      System.out.println("Waiting...");
      
      ControllerCommandsMXBean controller = connectToController(controllerPort);
      controller.hello(name);

      boolean run = true;
      while (run) {
        System.out.println(name + " is polling");
        Command work = controller.pull(name);
        switch (work.getAction()) {
        case DIE :
          System.out.println(name + " will die");
          run = false;
          controller.report(name, Status.OK);
          break;
        case ANALYSE :
          System.out.println(name + " is doing " + work);

          
          controller.report(name, Status.OK);
          break;
        case SELFCHECK :
          
        }
      }
             
      Thread.sleep(1000);
      
    //  controller.goodbye(name);
      
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } 
    
    System.out.println("bye bye " + name);
  }

  private static ControllerCommandsMXBean connectToController(int controllerPort) {
    JMXServiceURL url;
    try {
      url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://:" + controllerPort + "/jmxrmi");
      JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
      MBeanServerConnection connection = jmxc.getMBeanServerConnection();
      
      ControllerCommandsMXBean mbeanProxy = JMX.newMXBeanProxy(connection, ObjectName.getInstance("org.pitest.minion:type=ControllerCommands"), 
          ControllerCommandsMXBean.class, true);
      
      return mbeanProxy;
      

    } catch (IOException | NullPointerException | MalformedObjectNameException e) {
      throw Unchecked.translateCheckedException(e);
    }
  
    
  }

}
