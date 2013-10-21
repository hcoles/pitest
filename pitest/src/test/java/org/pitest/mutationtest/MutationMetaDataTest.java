package org.pitest.mutationtest;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.junit.Test;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MethodName;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;

public class MutationMetaDataTest {

  @Test
  public void shouldPartitionResultsByMutatedClass() {
    MutationResult a = makeResult("Foo", "a"); 
    MutationResult b = makeResult("Bar", "a"); 
    MutationResult c = makeResult("Foo", "b"); 
    MutationResult d = makeResult("Foo", "c"); 
    
    MutationMetaData testee = new MutationMetaData(Arrays.asList(a,b,c,d));
    Collection<ClassMutationResults> actual = testee.toClassResults();
    assertEquals(2,actual.size());
    Iterator<ClassMutationResults> it = actual.iterator();
    ClassMutationResults first = it.next();
    assertEquals(ClassName.fromString("Bar"),first.getMutatedClass());
    assertEquals(1,first.getMutations().size());
    ClassMutationResults second = it.next();
    assertEquals(ClassName.fromString("Foo"),second.getMutatedClass());
    assertEquals(3,second.getMutations().size());
  }

  @Test
  public void shouldNotCreateEmptyClassResultsObjects() {
    MutationMetaData testee = new MutationMetaData(Collections.<MutationResult>emptyList());
    assertEquals(0,testee.toClassResults().size());
  }
  
  private MutationResult makeResult(String clazz, String method) {
    Location location = Location.location(ClassName.fromString(clazz), MethodName.fromString(method), "()V");
    MutationIdentifier id =  new MutationIdentifier(location,1,"mutator");
    MutationDetails md = new MutationDetails(id, "file", "desc", 42, 0);
    final MutationResult mr = new MutationResult(md, new MutationStatusTestPair(0,
            DetectionStatus.KILLED));
    return mr;
  }
  
}
