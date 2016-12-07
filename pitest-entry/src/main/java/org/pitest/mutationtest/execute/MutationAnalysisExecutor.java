package org.pitest.mutationtest.execute;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.pitest.functional.FCollection;
import org.pitest.functional.SideEffect1;
import org.pitest.mutationtest.ClassMutationResults;
import org.pitest.mutationtest.MutationMetaData;
import org.pitest.mutationtest.MutationResultListener;
import org.pitest.mutationtest.build.MutationAnalysisUnit;
import org.pitest.util.Log;
import org.pitest.util.Unchecked;

public class MutationAnalysisExecutor {

  private static final Logger                LOG = Log.getLogger();

  private final List<MutationResultListener> listeners;
  private final ThreadPoolExecutor           executor;

  public MutationAnalysisExecutor(int numberOfThreads,
      List<MutationResultListener> listeners) {
    this.listeners = listeners;
    this.executor = new ThreadPoolExecutor(numberOfThreads, numberOfThreads,
        10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
        Executors.defaultThreadFactory());
  }

  // entry point for mutation testing
  public void run(final List<MutationAnalysisUnit> testUnits) {

    LOG.fine("Running " + testUnits.size() + " units");

    signalRunStartToAllListeners();

    List<Future<MutationMetaData>> results = new ArrayList<Future<MutationMetaData>>(
        testUnits.size());

    for (final MutationAnalysisUnit unit : testUnits) {
      results.add(this.executor.submit(unit));
    }

    this.executor.shutdown();

    try {
      processResult(results);
    } catch (InterruptedException e) {
      throw Unchecked.translateCheckedException(e);
    } catch (ExecutionException e) {
      throw Unchecked.translateCheckedException(e);
    }

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
