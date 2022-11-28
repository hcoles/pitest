package org.pitest.mutationtest.verify;

import java.util.List;

/**
 * Checks project for potential issues. Should throw an exception
 * for major issues detected with 100% confidence, for less certain
 * or lower priority issues log and return a string.
 */
public interface BuildVerifier {

  List<String> verify();

}
