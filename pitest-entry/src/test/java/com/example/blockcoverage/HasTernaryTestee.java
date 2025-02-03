package com.example.blockcoverage;

public class HasTernaryTestee {
  public static int mutable(int in) {
    return (in > 0 ? in++ : in--);
  }
}
