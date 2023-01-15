
package org.pitest.coverage.execute;
import org.pitest.testapi.TestUnit;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.io.Serializable;
public class TdgTestMethodResult implements Serializable{
    private static final long serialVersionUID = 1L;
    public Map<String, Set<String> > res = new HashMap<>();


    public TdgTestMethodResult(List<TestUnit> testUnits) {
        System.out.println("该项目一共有 "+testUnits.size() + "个测试单元");
        for (TestUnit tests : testUnits) {
            String testClass = tests.getDescription().getFirstTestClass();
            if (!res.containsKey(testClass)) {
                res.put(testClass, new HashSet<>());
            }    
                res.get(testClass).add(tests.getDescription().getName());
            
        }
    }

}
