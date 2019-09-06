package com.example.coverage.execute.samples.mutationMatrix;

import java.util.ArrayList;
import java.util.List;

public class SimpleCalculator {

  public static int sum(int x, int y) {
    return x + y;
  }

  public static void crash(boolean infinite) {
    final List<String[]> vals = new ArrayList<>();
    do {
      vals.add(new String[9999999]);
      vals.add(new String[9999999]);
      vals.add(new String[9999999]);
      vals.add(new String[9999999]);
    } while (infinite);
  }
}
