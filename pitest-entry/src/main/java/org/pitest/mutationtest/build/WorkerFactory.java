package org.pitest.mutationtest.build;

import org.pitest.classinfo.ClassName;
import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.EngineArguments;
import org.pitest.mutationtest.MutationConfig;
import org.pitest.mutationtest.TimeoutLengthStrategy;
import org.pitest.mutationtest.config.TestPluginArguments;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.execute.MinionArguments;
import org.pitest.mutationtest.execute.MutationTestProcess;
import org.pitest.process.ProcessArgs;
import org.pitest.util.Log;
import org.pitest.util.SocketFinder;
import org.pitest.util.Verbosity;

import java.io.File;
import java.util.Collection;
import java.util.function.Consumer;

import static org.pitest.functional.prelude.Prelude.printlnWith;

public class WorkerFactory {

  private final String                classPath;
  private final File                  baseDir;
  private final TestPluginArguments   pitConfig;
  private final TimeoutLengthStrategy timeoutStrategy;
  private final Verbosity             verbosity;
  private final boolean               fullMutationMatrix;
  private final MutationConfig        config;
  private final EngineArguments       args;

  public WorkerFactory(final File baseDir,
      final TestPluginArguments pitConfig,
      final MutationConfig mutationConfig,
      final EngineArguments args,
      final TimeoutLengthStrategy timeoutStrategy,
      final Verbosity verbosity,
      final boolean fullMutationMatrix,
      final String classPath) {
    this.pitConfig = pitConfig;
    this.timeoutStrategy = timeoutStrategy;
    this.verbosity = verbosity;
    this.fullMutationMatrix = fullMutationMatrix;
    this.classPath = classPath;
    this.baseDir = baseDir;
    this.config = mutationConfig;
    this.args = args;
  }

  public MutationTestProcess createWorker(
      final Collection<MutationDetails> remainingMutations,
      final Collection<ClassName> testClasses) {
    final MinionArguments fileArgs = new MinionArguments(remainingMutations,
        testClasses, this.config.getEngine().getName(), this.args, this.timeoutStrategy,
        Log.verbosity(), this.fullMutationMatrix, this.pitConfig);

    final ProcessArgs args = ProcessArgs.withClassPath(this.classPath)
        .andLaunchOptions(this.config.getLaunchOptions())
        .andBaseDir(this.baseDir).andStdout(captureStdOutIfVerbose())
        .andStderr(captureStdErrIfVerbose());

    final SocketFinder sf = new SocketFinder();
    return new MutationTestProcess(
        sf.getNextAvailableServerSocket(), args, fileArgs);
  }

  private Consumer<String> captureStdOutIfVerbose() {
    if (this.verbosity.showMinionOutput()) {
      return printlnWith("stdout ");
    } else {
      return Prelude.noSideEffect(String.class);
    }
  }

  private Consumer<String> captureStdErrIfVerbose() {
    if (this.verbosity.showMinionOutput()) {
      return printlnWith("stderr ");
    } else {
      return Prelude.noSideEffect(String.class);
    }
  }

}
