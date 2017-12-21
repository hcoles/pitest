package org.pitest.controller;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.pitest.coverage.TestInfo;
import org.pitest.minion.commands.Action;
import org.pitest.minion.commands.Command;
import org.pitest.minion.commands.MutId;
import org.pitest.minion.commands.Status;
import org.pitest.minion.commands.Test;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;

public class DeafultWorkScheduler implements WorkScheduler {
  
  private final BlockingQueue<MutationDetails> backlog = new LinkedBlockingQueue<>();
  private final Map<String, WorkItem> current = new ConcurrentHashMap<>();
  
  private final ResultListener listener;
  

  public DeafultWorkScheduler(Collection<MutationDetails> backlog, ResultListener listener) {
    this.listener = listener;
    this.backlog.addAll(backlog);
  }

  @Override
  public Command next(String worker) {
    WorkItem oldWi = current.get(worker);
    if (oldWi != null) {
      Command c = new Command(toOpenType(oldWi.id.getId()), oldWi.nextTest(), Action.ANALYSE);
      return c;
    } else {
      MutationDetails md = backlog.poll();
      if (md == null) {
        return Command.die();
        
      } else {
        WorkItem wi = new WorkItem();
        wi.id = md;
        wi.test = 0;
        current.put(worker, wi);   
        return new Command(toOpenType(wi.id.getId()), wi.nextTest(), Action.ANALYSE);    
      }
    }
  }
  
  @Override
  public void done(String worker, Command c, Status result) {
    WorkItem wi = current.get(worker);
    switch (c.getAction()) {
    case ANALYSE:
      if (result != Status.TEST_FAILED || !wi.testsRemain()) {
        current.remove(worker);
        reportResult(result, wi, c);
      }
      break;
    case DIE:
      current.remove(worker);
      if (backlog.isEmpty() && current.isEmpty()) {
        synchronized (this) {
          this.notifyAll();
        }
      }
      
      break;
    case SELFCHECK:
      throw new IllegalStateException("not deno yet");

    }

  }
  

  private static MutId toOpenType(MutationIdentifier id) {
    Location l = id.getLocation();
    return new MutId(id.getClassName().asJavaName(), l.getMethodName().name(),
        l.getMethodDesc(), id.getFirstIndex(), id.getMutator());
  }

  private void reportResult(Status result, WorkItem wi, Command c) {
    MutationStatusTestPair status = new MutationStatusTestPair(wi.test + 1, toMutationStatus(result), c.getTest().getName());
    MutationResult r = new MutationResult(wi.id, status);
    listener.report(r);
  }


  private DetectionStatus toMutationStatus(Status result) {
    switch (result) {
    case MEMORY_ERROR :
    return DetectionStatus.MEMORY_ERROR;
    case TIMED_OUT :
      return DetectionStatus.TIMED_OUT;
    case TEST_PASSED :
      return DetectionStatus.SURVIVED;
    case TEST_FAILED :
      return DetectionStatus.KILLED;
    default :
      throw new IllegalStateException();  
    }
  }

  //static class 
  class WorkItem {
    MutationDetails id;
    int test;
    
    Test nextTest() {
      TestInfo ti = id.getTestsInOrder().get(test);
      Test nextTest = new Test(ti.getDefiningClass(), ti.getName());
      test = test + 1;
      return nextTest;
    }
    
   
    boolean testsRemain() {
      return id.getTestsInOrder().size() > test;
    }
  }

  @Override
  public synchronized void awaitCompletion() {
    try {
      this.wait();
    } catch (InterruptedException e) {
      // swallow
    }
    
  }

  
}
