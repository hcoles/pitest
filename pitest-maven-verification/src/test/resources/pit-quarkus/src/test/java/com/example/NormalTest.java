package com.example;

import org.junit.jupiter.api.Test;

class NormalTest {

  @Test
  void dd() {
     long pid = ProcessHandle.current().pid();
      System.out.println("!!!!!!!!!!!!! " + pid);
  }

}
