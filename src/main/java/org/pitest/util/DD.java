package org.pitest.util;

//Simple Delta Debugging algorithm
//@author: Andreas Zeller
//$Id: DD.java,v 1.1 2004/12/01 16:36:53 zeller Exp $

import java.util.LinkedList;
import java.util.List;

import org.pitest.functional.F;

public class DD<T> {

  public enum Result {
    FAIL, PASS, UNRESOLVED
  }

  // List utilities
  // Split a configuration C into N subsets
  public List<List<T>> split(final List<T> c, final int n) {
    final List<List<T>> subSets = new LinkedList<List<T>>();
    int start = 0;

    for (int i = 0; i < n; i++) {
      final List<T> subSet = c.subList(start, start + (c.size() - start)
          / (n - i));
      subSets.add(subSet);
      start += subSet.size();
    }

    // System.out.println("split(" + c + ", " + n + ") = " + subSets);

    return subSets;
  }

  // Return a - b
  public List<T> minus(final List<T> a, final List<T> b) {
    final List<T> c = new LinkedList<T>();

    // Rather inefficient right now
    for (final T element : a) {
      if (!b.contains(element)) {
        c.add(element);
      }
    }

    // System.out.println("minus(" + a + ", " + b + ") = " + c);

    return c;
  }

  // Return a \cup b
  public List<T> union(final List<T> a, final List<T> b) {
    final List<T> c = new LinkedList<T>();

    for (final T element : a) {
      c.add(element);
    }

    for (final T element : b) {
      c.add(element);
    }

    // System.out.println("union(" + a + ", " + b + ") = " + c);

    return c;
  }

  // ddmin algorithm
  // Return a sublist of CIRCUMSTANCES that is a relevant
  // configuration with respect to TEST.
  public List<T> ddmin(final List<T> circumstances_,
      final F<List<T>, Result> test) {
    System.out.println("ddmin(" + circumstances_ + ")...");

    List<T> circumstances = circumstances_;

    assert test.apply(new LinkedList<T>()) == Result.PASS;
    assert test.apply(circumstances) == Result.FAIL;

    int n = 2;

    while (circumstances.size() >= 2) {
      final List<List<T>> subsets = split(circumstances, n);
      assert subsets.size() == n;

      System.out.println("ddmin: testing subsets");

      boolean some_complement_is_failing = false;
      for (int i = 0; i < subsets.size(); i++) {
        final List<T> subset = subsets.get(i);
        final List<T> complement = minus(circumstances, subset);

        if (test.apply(complement) == Result.FAIL) {
          circumstances = complement;
          n = Math.max(n - 1, 2);
          some_complement_is_failing = true;
          break;
        }
      }

      if (!some_complement_is_failing) {
        if (n == circumstances.size()) {
          break;
        }

        System.out.println("ddmin: increasing granularity");
        n = Math.min(n * 2, circumstances.size());
      }
    }

    System.out.println("ddmin(" + circumstances_ + ") = " + circumstances);

    return circumstances;
  }

  // ddiso algorithm
  // Return a triple (DELTA, C_PASS', C_FAIL') such that
  // C_PASS subseteq C_PASS' subset C_FAIL' subseteq C_FAIL holds
  // DELTA = C_FAIL' - C_PASS' is a minimal difference relevant for TEST.
  public List<List<T>> ddiso(final List<T> c_pass_, final List<T> c_fail_,
      final F<List<T>, Result> test) {
    System.out.println("ddiso(" + c_pass_ + ", " + c_fail_ + ")...");

    List<T> c_pass = c_pass_;
    List<T> c_fail = c_fail_;

    int n = 2;

    while (true) {
      assert test.apply(c_pass) == Result.PASS;
      assert test.apply(c_fail) == Result.FAIL;

      final List<T> delta = minus(c_fail, c_pass);
      if (n > delta.size()) {
        final List<List<T>> ret = new LinkedList<List<T>>();
        ret.add(delta);
        ret.add(c_pass);
        ret.add(c_fail);

        System.out.println("ddiso(" + c_pass_ + ", " + c_fail_ + ") = " + ret);
        return ret;
      }

      final List<List<T>> deltas = split(delta, n);
      assert deltas.size() == n;

      int offset = 0;
      int j = 0;

      while (j < n) {
        final int i = (j + offset) % n;
        final List<T> next_c_pass = union(c_pass, deltas.get(i));
        final List<T> next_c_fail = minus(c_fail, deltas.get(i));

        if ((test.apply(next_c_fail) == Result.FAIL) && (n == 2)) {
          c_fail = next_c_fail;
          n = 2;
          offset = 0;
          break;
        } else if (test.apply(next_c_fail) == Result.PASS) {
          c_pass = next_c_fail;
          n = 2;
          offset = 0;
          break;
        } else if (test.apply(next_c_pass) == Result.FAIL) {
          c_fail = next_c_pass;
          n = 2;
          offset = 0;
          break;
        } else if (test.apply(next_c_fail) == Result.FAIL) {
          c_fail = next_c_fail;
          n = Math.max(n - 1, 2);
          offset = i;
          break;
        } else if (test.apply(next_c_pass) == Result.PASS) {
          c_pass = next_c_pass;
          n = Math.max(n - 1, 2);
          offset = i;
          break;
        } else {
          j++;
        }
      }

      if (j >= n) {
        if (n >= delta.size()) {
          final List<List<T>> ret = new LinkedList<List<T>>();
          ret.add(delta);
          ret.add(c_pass);
          ret.add(c_fail);

          System.out
              .println("ddiso(" + c_pass_ + ", " + c_fail_ + ") = " + ret);
        } else {
          System.out.println("ddmin: increasing granularity");
          n = Math.min(n * 2, delta.size());
        }
      }
    }
  }
}

class DemoDD extends DD<Integer> {

  public static final Integer ONE   = new Integer(1);
  public static final Integer TWO   = new Integer(2);
  public static final Integer THREE = new Integer(3);
  public static final Integer FOUR  = new Integer(4);

  public static void main(final String[] args) {
    final LinkedList<Integer> config = new LinkedList<Integer>();

    config.add(ONE);
    config.add(TWO);
    config.add(THREE);
    config.add(FOUR);
    System.out.println(config);

    final DD<Integer> mydd = new DemoDD();

    System.out.println("Running ddmin");

    final F<List<Integer>, Result> f = new F<List<Integer>, Result>() {

      public org.pitest.util.DD.Result apply(final List<Integer> a) {
        if (a.contains(ONE) && a.contains(THREE)) {
          return Result.FAIL;
        }
        return Result.PASS;
      }

    };

    mydd.ddmin(config, f);

    System.out.println("");
    System.out.println("Running ddiso");
    mydd.ddiso(new LinkedList<Integer>(), config, f);
  }
}