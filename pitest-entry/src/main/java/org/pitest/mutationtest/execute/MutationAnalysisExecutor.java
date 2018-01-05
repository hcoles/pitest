package org.pitest.mutationtest.execute;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.pitest.controller.Controller;
import org.pitest.controller.ResultListener;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.ClassMutationResults;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationAnalyser;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationResultListener;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.TimeoutLengthStrategy;
import org.pitest.mutationtest.config.TestPluginArguments;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.process.LaunchOptions;
import org.pitest.util.Log;

public class MutationAnalysisExecutor {

  private static final Logger                LOG = Log.getLogger();

  private final String                classPath;
  private final File                  baseDir;
  private final TestPluginArguments   pitConfig;
  private final MutationEngineArguments         mutationArgs;

  private final boolean               verbose;
  private final LaunchOptions         config;
  private final int threads;
  
  private final MutationResultListener listener;

  private final MutationAnalyser analyser;
  private final TimeoutLengthStrategy timeout;
  
  
  public MutationAnalysisExecutor(
      MutationAnalyser analyser,
      int threads, MutationResultListener listener,
      String classPath, File baseDir, TestPluginArguments pitConfig,
      MutationEngineArguments mutationArgs, boolean verbose, LaunchOptions config, TimeoutLengthStrategy timeout) {
    this.analyser = analyser;
    this.listener = listener;
    this.classPath = classPath;
    this.baseDir = baseDir;
    this.pitConfig = pitConfig;
    this.mutationArgs = mutationArgs;
    this.verbose = verbose;
    this.config = config;
    this.threads = threads;
    this.timeout = timeout;
  }


  
  // entry point for mutation testing
  public void run(final List<MutationDetails> mutations) {

    LOG.fine("Analysing " + mutations.size() + " mutations");

    listener.runStart();
    
    Collection<MutationResult> analysedMutations = FCollection.map(analyser.analyse(mutations), uncovered());
    
    final Collection<MutationDetails> needAnalysis = FCollection.filter(
        analysedMutations, statusNotKnown()).map(resultToDetails());

    final List<MutationResult> analysed = FCollection.filter(analysedMutations,
        Prelude.not(statusNotKnown()));
    
    reportReadyAnalysedResults(analysed);

    // What thread are results coming in on?
    // do we need to collect them to the main thread?

    Controller controller = new Controller(threads, classPath, baseDir, pitConfig, mutationArgs, verbose, config, timeout);
    controller.process(needAnalysis, listen());
    

    listener.runEnd();

  }


  private F<MutationResult, MutationResult> uncovered() {
    return new F<MutationResult, MutationResult> () {
      @Override
      public MutationResult apply(MutationResult a) {
        if (a.getStatus().equals(DetectionStatus.NOT_STARTED) && a.getDetails().getTestsInOrder().isEmpty()) {
          return new MutationResult(a.getDetails(), new MutationStatusTestPair(0,DetectionStatus.NO_COVERAGE) );
        }
        return a;
      }
      
    };
  }


  private void reportReadyAnalysedResults(final List<MutationResult> analysed) {
    for (MutationResult each : analysed ) {
      ClassMutationResults cmr = new ClassMutationResults(Collections.singletonList(each));
      listener.handleMutationResult(cmr);
    }
  }
  
  
  private static F<MutationResult, MutationDetails> resultToDetails() {
    return new F<MutationResult, MutationDetails>() {
      @Override
      public MutationDetails apply(final MutationResult a) {
        return a.getDetails();
      }
    };
  }

  private static F<MutationResult, Boolean> statusNotKnown() {
    return new F<MutationResult, Boolean>() {
      @Override
      public Boolean apply(final MutationResult a) {
        return a.getStatus() == DetectionStatus.NOT_STARTED;
      }
    };
  }

  

  private ResultListener listen() {
    return new ResultListener() {
      @Override
      public void report(MutationResult r) {
          ClassMutationResults cmr = new ClassMutationResults(Collections.singletonList(r));
          listener.handleMutationResult(cmr);        
      }  
    };
  }


}
