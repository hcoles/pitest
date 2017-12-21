package org.pitest.controller;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.pitest.functional.FCollection;
import org.pitest.functional.SideEffect1;
import org.pitest.mutationtest.ClassMutationResults;
import org.pitest.mutationtest.MutationMetaData;
import org.pitest.mutationtest.MutationResultListener;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.util.Log;

public class Executor {

  private static final Logger                LOG = Log.getLogger();

  private final List<MutationResultListener> listeners;

  public Executor(int numberOfThreads, List<MutationResultListener> listeners) {
    this.listeners = listeners;
  }

  // entry point for mutation testing
  public void run(final List<MutationDetails> toAnalyse, int threads) {

    LOG.fine("Anaylsing " + toAnalyse.size() + " mutants");

    signalRunStartToAllListeners();



    signalRunEndToAllListeners();

  }

  private void processResult(List<Future<MutationMetaData>> results)
      throws InterruptedException, ExecutionException {
    for (Future<MutationMetaData> f : results) {
      MutationMetaData r = f.get();
      for (MutationResultListener l : this.listeners) {
        for (final ClassMutationResults cr : r.toClassResults()) {
          l.handleMutationResult(cr);
        }
      }
    }
  }

  private void signalRunStartToAllListeners() {
    FCollection.forEach(this.listeners,
        new SideEffect1<MutationResultListener>() {
          @Override
          public void apply(final MutationResultListener a) {
            a.runStart();
          }
        });
  }

  private void signalRunEndToAllListeners() {
    FCollection.forEach(this.listeners,
        new SideEffect1<MutationResultListener>() {
          @Override
          public void apply(final MutationResultListener a) {
            a.runEnd();
          }
        });
  }

}
