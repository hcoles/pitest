package org.pitest.coverage;

import java.util.List;
import java.util.stream.Collectors;

public class CompoundTestStatListener implements TestStatListener {

    private final List<TestStatListener> children;

    public CompoundTestStatListener(List<TestStatListener> listeners) {
        this.children = listeners;
    }

    @Override
    public void accept(CoverageResult cr) {
        children.forEach(c -> c.accept(cr));
    }

    @Override
    public List<String> messages() {
        return children.stream()
                .flatMap(c -> c.messages().stream())
                .collect(Collectors.toList());
    }

    @Override
    public void end() {
        children.forEach(TestStatListener::end);
    }
}
