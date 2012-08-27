package org.pitest.mutationtest.incremental;

import java.util.Collection;
import java.util.Map;

import org.pitest.classinfo.ClassIdentifier;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.execute.MutationStatusTestPair;
import org.pitest.mutationtest.results.MutationResult;

public class NullHistoryStore implements HistoryStore {

  public void initialize() {
    // TODO Auto-generated method stub
    
  }

  public void recordClassPath(Collection<ClassIdentifier> ids) {
    // TODO Auto-generated method stub
    
  }

  public void recordResult(MutationResult result) {
    // TODO Auto-generated method stub
    
  }

  public Map<MutationIdentifier, MutationStatusTestPair> getHistoricResults() {
    // TODO Auto-generated method stub
    return null;
  }

  public Map<ClassName, ClassIdentifier> getHistoricClassPath() {
    // TODO Auto-generated method stub
    return null;
  }



}
