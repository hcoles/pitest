package org.pitest.mutationtest.tdg.execute;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
public class TdgResult {
    private Map<String, Set<String>> clazzAndMethodNames = new HashMap<>();
    TdgResult(String className, Set<String> methodNames) {
        clazzAndMethodNames.put(className, methodNames);
    }

    public Map<String, Set<String>> getclazzAndMethodNames() {
        return this.clazzAndMethodNames;
    }

}
