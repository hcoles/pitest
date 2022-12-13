package org.pitest.mutationtest.tdghistory;
import org.pitest.util.CheckSumUtil;
import java.net.URL;
import java.util.Map;
import java.util.Collection;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.classinfo.ClassName;
import java.util.Optional;
import java.util.HashMap;
import org.pitest.mutationtest.tdg.Tdg;
import org.pitest.coverage.TestInfo;
import org.pitest.classpath.ProjectClassPaths;
import org.pitest.util.CheckSumUtil;
public class TdgCodeHistory {
    private Map<URL, String>                                       previousHash = new HashMap<>();
    private  Map<MutationIdentifier, MutationStatusTestPair> previousResults   = new HashMap<>();
    ProjectClassPaths classPath;
    Tdg tdg;
    CheckSumUtil checkSumUtil = new CheckSumUtil();
    public TdgCodeHistory(TdgHistoryStore tdgHistory, ProjectClassPaths classPath, Tdg tdg) {
        this.previousHash = tdgHistory.getHistorySha();
        this.previousResults = tdgHistory.getHistoricResults();
        this.classPath = classPath;
        this.tdg = tdg;
    }

    public boolean hasClassChanged(ClassName name) {
        URL url = this.classPath.getClassPath().findResource(name.asInternalName() + ".class");
        String hashcode = this.checkSumUtil.getCheckSum(url);
        if (!this.previousHash.containsKey(url)) return true;
        return this.previousHash.get(url) == hashcode;
    }

    public boolean hasTestsChanged(ClassName targetClassName) {
        Collection<TestInfo>  relatedTests = this.tdg.getTests(targetClassName);
        for (TestInfo test : relatedTests) {
            if (this.hasClassChanged(TestInfo.toDefiningClassName().apply(test))) return true;
        }
        return false;
    }

    public Optional<MutationStatusTestPair> getPreviousResult(MutationIdentifier id) {
        return Optional.ofNullable(this.previousResults.get(id));
    }
}
