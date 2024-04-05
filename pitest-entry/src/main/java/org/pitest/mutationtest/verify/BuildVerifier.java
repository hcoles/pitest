package org.pitest.mutationtest.verify;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Checks project for potential issues. Should throw an exception
 * for major issues detected with 100% confidence, for less certain
 * or lower priority issues log and return a string.
 */
public interface BuildVerifier {

  @Deprecated
  default List<String> verify() {
    return Collections.emptyList();
  }

  default List<BuildMessage> verifyBuild() {
    return verify().stream()
            .map(BuildMessage::buildMessage)
            .collect(Collectors.toList());
  }

}
