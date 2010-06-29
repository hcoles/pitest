package org.pitest.mutationtest;

public class InfiniteLoop {

  public void loop() {
    int i = 1;
    do {
      i++;
      System.out.println("loop");
    } while (i != 1);
  }

}
