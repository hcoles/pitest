package com.example;

public class CrashesJVMWhenMutated {

  public static void crashJVM(int i) {

    if (i == 0) {
      crashJVM();
    }
 

  }
  // crashes Sun Java 5, 6 and OpenJDK 6 on Ubuntu
  // and hopefully (?) other OS' 
  // see http://stackoverflow.com/questions/65200/how-do-you-crash-a-jvm
  private static void crashJVM() {
    
    Object[] o = null;

    while (true) {
      o = new Object[] { o };
    }
  }

}
