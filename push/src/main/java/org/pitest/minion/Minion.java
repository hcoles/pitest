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

    try {
      int controllerPort = Integer.parseInt(args[0]);
      String name = args[1];
            
      System.out.println("Waiting...");
      
      ControllerCommandsMXBean controller = connectToController(controllerPort);
      controller.hello(name);

      boolean run = true;
      while (run) {
        Command work = controller.pull(name);
        switch (work.getAction()) {
        case DIE :
          System.out.println(name + " will die");
          run = false;
          break;
        case ANALYSE :
          System.out.println(name + " is doing " + work);
          if (work.getId().getOperator().equals("death")) {
            for (int i = 0; i != Integer.MIN_VALUE; i++) {
              Thread.sleep(1);
              //System.out.println("s");
            }
          } else {
            Thread.sleep(100);
          }
          
          controller.report(name, Status.OK);
          break;
        case SELFCHECK :
          
        }
      }
             
      Thread.sleep(1000);
      
      controller.goodbye(name);
      
  //    server.stop();
      
     // Thread.sleep(Long.MAX_VALUE);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } 
    
    System.out.println("bye bye");
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
