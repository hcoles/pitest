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

import org.pitest.mutationtest.ClassMutationResults;
import org.pitest.mutationtest.MutationMetaData;
import org.pitest.mutationtest.MutationResultInterceptor;
import org.pitest.mutationtest.MutationResultListener;
import org.pitest.mutationtest.build.MutationAnalysisUnit;
import org.pitest.util.Log;
import org.pitest.util.Unchecked;

public class MutationAnalysisExecutor {

  private static final Logger                LOG = Log.getLogger();

  private final List<MutationResultListener> listeners;
  private final ThreadPoolExecutor           executor;

  private final MutationResultInterceptor resultInterceptor;

  public MutationAnalysisExecutor(int numberOfThreads, MutationResultInterceptor interceptor,
      List<MutationResultListener> listeners) {
    this.resultInterceptor = interceptor;
    this.listeners = listeners;
    this.executor = new ThreadPoolExecutor(numberOfThreads, numberOfThreads,
        10, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
        Executors.defaultThreadFactory());
  }

  // entry point for mutation testing
  public void run(final List<MutationAnalysisUnit> testUnits) {

    LOG.fine("Running " + testUnits.size() + " units");

    signalRunStartToAllListeners();

    final List<Future<MutationMetaData>> results = new ArrayList<>(
        testUnits.size());

    for (final MutationAnalysisUnit unit : testUnits) {
      results.add(this.executor.submit(unit));
    }

    this.executor.shutdown();

    try {
      processResult(results);
    } catch (final InterruptedException | ExecutionException e) {
      throw Unchecked.translateCheckedException(e);
    }

    signalRunEndToAllListeners();

  }

  private void processResult(List<Future<MutationMetaData>> results)
          throws InterruptedException, ExecutionException {
    for (Future<MutationMetaData> f : results) {
      MutationMetaData metaData = f.get();
      for (ClassMutationResults cr : resultInterceptor.modify(metaData.toClassResults())) {
        for (MutationResultListener listener : this.listeners) {
          listener.handleMutationResult(cr);
        }
      }
    }

    // handle any results held back from processing. Only known
    // use case here is inlined code consolidation.
    for (ClassMutationResults each : resultInterceptor.remaining()) {
      for (MutationResultListener listener : this.listeners) {
        listener.handleMutationResult(each);
      }
    }

  }

  private void signalRunStartToAllListeners() {
    this.listeners.forEach(MutationResultListener::runStart);
  }

  private void signalRunEndToAllListeners() {
    this.listeners.forEach(MutationResultListener::runEnd);
  }

}
