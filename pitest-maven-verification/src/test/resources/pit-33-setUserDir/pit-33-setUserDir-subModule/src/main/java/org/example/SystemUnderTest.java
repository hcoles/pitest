package org.example;

public class SystemUnderTest {
  
  private int aNumber = 0;
  
  public SystemUnderTest() {
    super();
    
    int a = 25;
    int b = 10;
    if (a < b) {
      aNumber = 10;
    } else {
      aNumber = -25;
    }
  }
  
  public String toString() {
    return "SystemUnderTest";
  }
  
  
}