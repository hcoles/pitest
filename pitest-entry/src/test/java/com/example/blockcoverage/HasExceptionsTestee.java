package com.example.blockcoverage;

public class HasExceptionsTestee {
  public static void foo(){
    String x = null;
    int y  =x.length();
    y++;
  }
}
