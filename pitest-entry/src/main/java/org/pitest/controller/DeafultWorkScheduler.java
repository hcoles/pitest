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
import org.pitest.mutationtest.TimeoutLengthStrategy;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;

public class DeafultWorkScheduler implements WorkScheduler {
  
  private final BlockingQueue<MutationDetails> backlog = new LinkedBlockingQueue<>();
  private final Map<String, WorkItem> current = new ConcurrentHashMap<>();
  
  private final ResultListener listener;
  private final TimeoutLengthStrategy timeoutCalc;

  public DeafultWorkScheduler(Collection<MutationDetails> backlog, ResultListener listener, TimeoutLengthStrategy tls) {
    this.listener = listener;
    this.backlog.addAll(backlog);
    this.timeoutCalc = tls;
  }

  @Override
  public Work next(String worker) {
    WorkItem oldWi = current.get(worker);
    if (oldWi != null) {
      return makeWork(oldWi);
    } else {
      MutationDetails md = backlog.poll();
      if (md == null) {
        current.put(worker, new WorkItem()); // fixme need to represent death
        return Work.untimed(Command.die());
        
      } else {
        WorkItem wi = new WorkItem();
        wi.id = md;
        wi.test = 0;
        current.put(worker, wi);  
        return makeWork(wi);
        
      }
    }
  }

  private Work makeWork(WorkItem oldWi) {
    TestInfo ti = oldWi.nextTest();
    Test t = new Test(ti.getDefiningClass(), ti.getName());
    Command c = new Command(toOpenType(oldWi.id.getId()), t, Action.ANALYSE);
    int timeAllowed = (int)timeoutCalc.getAllowedTime(ti.getTime());
    return Work.of(timeAllowed, c);
  }
  
  @Override
  public void done(String worker, Command c, Status result) {
    WorkItem wi = current.get(worker);
    switch (c.getAction()) {
    case ANALYSE:
      if (wi == null ) {
        throw new IllegalStateException("No work found for " + worker + " but reported " + c + " and " + result);
      }
      
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
    MutationStatusTestPair status = new MutationStatusTestPair(wi.test, toMutationStatus(result), c.getTest().getName());
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
    case UNEXPECTED_ERROR :
      return DetectionStatus.RUN_ERROR;
    default :
      throw new IllegalStateException();  
    }
  }

  //static class 
  class WorkItem {
    MutationDetails id;
    int test;
    
    TestInfo nextTest() {
      TestInfo ti = id.getTestsInOrder().get(test);
      //Test nextTest = new Test(ti.getDefiningClass(), ti.getName());
      test = test + 1;
      //return nextTest;
      return ti;
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
