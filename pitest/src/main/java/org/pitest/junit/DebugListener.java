package org.pitest.junit;

import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Extracts failure strings from junit error runners
 * Currently only for initialisation errors
 */
public class DebugListener extends RunListener {
    private final List<String> problems = new ArrayList<>();

   @Override
    public void testFailure(Failure failure) {
       String exception = failure.getException().toString();
       if (exception.contains("NoClassDefFoundError") || exception.contains("ClassNotFoundException")) {
           problems.add(exception + "\n" + failure.getTrace());
       }

    }

    public Optional<String> problems() {
       if (problems.isEmpty()) {
           return Optional.empty();
       }
       return Optional.of(String.join("\n",problems));
    }

}
