package org.pitest.controller;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.pitest.minion.commands.Action;
import org.pitest.minion.commands.Command;
import org.pitest.minion.commands.MutId;
import org.pitest.minion.commands.Status;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MutationIdentifier;

public class MinionPool {
  
  private final int size;
  private final MinionFactory factory;
  
  private final Map<String, Process> starting = new ConcurrentHashMap<>();
  private final Map<String, MinionHandle> minions = new ConcurrentHashMap<>();
  
  private final Map<String, Job> inProgress = new ConcurrentHashMap<>();
  private final ConcurrentLinkedQueue<MutationIdentifier> work = new ConcurrentLinkedQueue<>();
  
  public MinionPool(int size, MinionFactory factory) {
    this.size = size;
    this.factory = factory;
  }
  
  public void start() throws IOException {
    for (int i = 0; i != size; i++) {
      factory.requestNewMinion(this);
    }
  }
  
  public void pergeZombies() {
    long now = System.currentTimeMillis();
    for (Entry<String, Job> each : inProgress.entrySet()) {
      if (each.getValue().isOverdue(now)) {
        MinionHandle handle = minions.get(each.getKey());
        handle.kill();
        System.out.println(each.getValue().work.getId() + " timed out");
        inProgress.remove(each.getKey());
        minions.remove(each.getKey());
        factory.requestNewMinion(this);
      }
    }
  }

  public void invite(String name, Process process) {
    starting.put(name, process);
  }
  
  public void join(String name) {
    Process p = starting.get(name);
    MinionHandle mh = new MinionHandle(p);
    minions.put(name, mh);
    starting.remove(name);
  }
  
  public void submit(Collection<MutationIdentifier> work) {
    this.work.addAll(work);
  }

  public Command assign(String name) {
    MutationIdentifier id = work.poll();
    if (id != null) {
      Command c = new Command(toOpenType(id), "", Action.ANALYSE);
      inProgress.put(name, new Job(System.currentTimeMillis(), c, 1000));
      return c;
    } else {
      return Command.die();
    }
  }
  
  public void report(String name, Status status) {
    Job job = inProgress.remove(name);
    System.out.println(job.work.getId() + " done"); 
  }
  
  
  public void unassignMinion(String name) {
    Job job = inProgress.remove(name);
    if (job != null) {
      System.out.println(job.work + " was poison");
    }
    MinionHandle handle = minions.remove(name);
    // just to make sure
    handle.kill();   
  }
  

  
 static class Job {
   long startTime;
   int allowedDuration;
   Command work;
   
   Job(long startTime, Command work, int allowedDuration) {
     this.startTime = startTime;
     this.work = work;     
   }
   
   boolean isOverdue(long now) {
     return now > (startTime + allowedDuration);
   }
 }


  private static MutId toOpenType(MutationIdentifier id) {
    Location l = id.getLocation();
    return new MutId(id.getClassName().asJavaName(), l.getMethodName().name(),
        l.getMethodDesc(), id.getFirstIndex(), id.getMutator());
  }

}
