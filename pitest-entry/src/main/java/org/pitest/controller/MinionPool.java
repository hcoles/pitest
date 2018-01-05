package org.pitest.controller;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.pitest.minion.commands.Action;
import org.pitest.minion.commands.Command;
import org.pitest.minion.commands.Status;

public class MinionPool {

  private final int                       size;
  private final MinionFactory             factory;

  private final Map<String, Process>      starting   = new ConcurrentHashMap<>();
  private final Map<String, MinionHandle> minions    = new ConcurrentHashMap<>();
  private final Map<String, Job>          inProgress = new ConcurrentHashMap<>();
  private final WorkScheduler             planner;

  public MinionPool(int size, MinionFactory factory, WorkScheduler planner) {
    this.size = size;
    this.factory = factory;
    this.planner = planner;
  }

  public void start() throws IOException {
    for (int i = 0; i != this.size; i++) {
      this.factory.requestNewMinion(this);
    }
  }

  public void pergeZombies() {
    System.out.println("Starting " + starting.size());
    System.out.println("inProgress " + inProgress.size());
    System.out.println("minions " + minions.size());
    
    final long now = System.currentTimeMillis();
    for (final Entry<String, Job> each : this.inProgress.entrySet()) {
      final MinionHandle handle = this.minions.get(each.getKey());
      boolean isAlive = handle.isAlive();
      if (!isAlive) {
        System.out.println(each.getKey() + " has dissapeared");
        this.planner.done(each.getKey(), each.getValue().work,
            Status.UNEXPECTED_ERROR);

        unassignMinion(each.getKey());
        this.factory.requestNewMinion(this);
      } else if (each.getValue().isOverdue(now)) {
          handle.kill();
          System.out.println(each.getValue().work + " timed out");

          this.planner.done(each.getKey(), each.getValue().work,
              Status.TIMED_OUT);

          unassignMinion(each.getKey());
          this.factory.requestNewMinion(this);
      } else {
        System.out.println("working : " + inProgress.size() );
      }
    }
  }

  public void invite(String name, Process process) {
    this.starting.put(name, process);
  }

  public void join(String name) {
    final Process p = this.starting.get(name);
    final MinionHandle mh = new MinionHandle(p);
    this.minions.put(name, mh);
    this.starting.remove(name);
  }

  public Command next(String name) {
    final Work w = this.planner.next(name);
    // FIXME need to size command execution times.
    this.inProgress.put(name, new Job(System.currentTimeMillis(), w.command(), w.duration()));
    return w.command();
  }

  public void report(String name, Status status) {
    final Job job = this.inProgress.remove(name);
    //System.out.println(job.work.getId() + " done " + " by " + name);
    this.planner.done(name, job.work, status);

    if (job.work.getAction() == Action.DIE) {
      unassignMinion(name);
    }

  }

  private void unassignMinion(String name) {
    System.out.println("Unassiging " + name);
    final Job job = this.inProgress.remove(name);
    if (job != null) {
      System.out.println(job.work + " was poison");
    }
    final MinionHandle handle = this.minions.remove(name);
    // just to make sure
    handle.kill();
  }

  static class Job {
    long    startTime;
    int     allowedDuration;
    Command work;

    Job(long startTime, Command work, int allowedDuration) {
      this.startTime = startTime;
      this.work = work;
    }

    boolean isOverdue(long now) {
      return now > (this.startTime + this.allowedDuration);
    }
  }

}
