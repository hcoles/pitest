package com.example;

public class CrashesJVMWhenMutated {

  public static void crashJVM(final int i) {
    if (i == 0) {
      crashJVM();
    }
  }

  private static void crashJVM() {
    Runtime.getRuntime().halt(2);

    // crashes Sun Java 5, but not later versions of 6
    // and hopefully (?) other OS'
    // see http://stackoverflow.com/questions/65200/how-do-you-crash-a-jvm
    // Object[] o = null;
    // while (true) {
    // o = new Object[] { o };
    // }
  }

}
